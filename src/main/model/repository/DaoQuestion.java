package main.model.repository;

import main.model.entity.Question;
import main.model.entity.Response;
import main.model.entity.Topic;
import main.model.entity.dto.QuestionRequest;
import main.model.entity.dto.QuestionResponse;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class DaoQuestion {
    private static final Logger logger = Logger.getLogger(DaoQuestion.class.getName());
    private final String questionSelectQuery = "SELECT q.id as question_id, q.difficultyRankNumber as difficulty, q.content, t.topicName as topic, r.text, r.correct FROM question_response qr JOIN question q ON question_id = q.id JOIN response r ON response_id = r.id JOIN topic t ON topic_id = t.id ";
    private final Connection connection;
    private final DaoTopic daoTopic;
    private final DaoResponse daoResponse;

    public DaoQuestion(Connection connection, DaoTopic daoTopic, DaoResponse daoResponse) {
        this.connection = connection;
        this.daoTopic = daoTopic;
        this.daoResponse = daoResponse;
    }

    /**
     * Retrieves all questions from the Database
     *
     * @return List of QuestionResponse Objects or empty List if none were found.
     */
    public List<QuestionResponse> retrieveQuestions() {
        try {
            PreparedStatement statement = connection.prepareStatement(questionSelectQuery);
            ResultSet rs = statement.executeQuery();

            return parseResultSet(rs);


        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Failed to retrieve all Questions: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Accumulates Responses into a List of Response Objects from the ResultSet and returns the unique QuestionResponse Objects
     *
     * @param rs ResultSet of the Query | Labels: question_id, text, correct, topic, difficulty, content
     * @return List of QuestionResponse Objects
     * @throws SQLException if specified column Labels of ResultSet are invalid
     */
    private static List<QuestionResponse> parseResultSet(ResultSet rs) throws SQLException {
        Map<Integer, QuestionResponse> retrievedQuestions = new HashMap<>();
        while (rs.next()) {
            int questionId = rs.getInt("question_id");

            Response response = new Response(
                    rs.getString("text"),
                    rs.getBoolean("correct")
            );

            Topic topic = Topic.valueOf(rs.getString("topic").toUpperCase());
            int difficultyRankNumber = rs.getInt("difficulty");
            String content = rs.getString("content");

            QuestionResponse question = new QuestionResponse(
                    questionId,
                    topic,
                    difficultyRankNumber,
                    content,
                    new ArrayList<>()
            );

            List<Response> responses = retrievedQuestions.getOrDefault(
                    questionId, question).responses();
            responses.add(response);

            retrievedQuestions.put(questionId, new QuestionResponse(
                    questionId,
                    topic,
                    difficultyRankNumber,
                    content,
                    responses
            ));
        }
        return retrievedQuestions.values().stream().toList();
    }

    /**
     * Saves question to the Database
     * Note: This should be wrapped in a Transaction for Production!
     * @return success
     */
    public Long saveQuestion(QuestionRequest request) {
        String questionInsertQuery = "INSERT INTO question(difficultyRankNumber,content,topic_id) VALUES(?,?,?)";

        try (
             PreparedStatement questionInsertStatement = connection.prepareStatement(questionInsertQuery, Statement.RETURN_GENERATED_KEYS);
        ) {

            //Insert Responses
            List<Long> responseIdList = daoResponse.saveResponses(request.responses());

            //Query Topic
            int topicId = daoTopic.getTopicIdByName(request.topic());

            //Insert Question
            questionInsertStatement.setInt(1, request.difficultyRankNumber());
            questionInsertStatement.setString(2, request.content());
            questionInsertStatement.setInt(3, topicId);

            questionInsertStatement.execute();
            ResultSet questionKeyResultSet = questionInsertStatement.getGeneratedKeys();
            questionKeyResultSet.next();
            long questionId = questionKeyResultSet.getLong(1);

            //Link question_response
            linkQuestionResponse(questionId, responseIdList);

            return questionId;

        } catch (SQLException e) {
            logger.severe("Failed to save Question: " + e.getMessage());
            return null;
        }
    }


    /**
     * Updates a Question and all related repositories
     * @param id The Question-Id to be updated
     * @param newQuestion QuestionRequest Object to update the Question with
     * @return success
     */
    public boolean updateQuestionById(long id, QuestionRequest newQuestion) {
        String questionUpdateQuery = "UPDATE question SET difficultyRankNumber = ?, content = ?";

        try {
            Optional<QuestionResponse> retrievedQuestion = findQuestionById(id);
            if (retrievedQuestion.isEmpty()) return false;
            QuestionResponse oldQuestion = retrievedQuestion.get();

            List<String> targetFields = Question.findDifference(
                    Question.getQuestion(newQuestion),
                    Question.getQuestion(oldQuestion)
            );

            //Adjust query String based on field to update:
            if (targetFields.contains("topic")) {
                int topicId = daoTopic.getTopicIdByName(newQuestion.topic());
                questionUpdateQuery = questionUpdateQuery.concat(", topic_id = " + topicId);
            }

            PreparedStatement questionUpdateStatement = connection.prepareStatement(questionUpdateQuery + " WHERE id = ?");

            questionUpdateStatement.setInt(1, newQuestion.difficultyRankNumber());
            questionUpdateStatement.setString(2, newQuestion.content());
            questionUpdateStatement.setLong(3, id);
            questionUpdateStatement.executeUpdate();
            questionUpdateStatement.close();

            if (targetFields.contains("responses")) {
                daoResponse.deleteQuestionResponsesForId(id);
                List<Long> updatedResponseList = daoResponse.saveResponses(newQuestion.responses());
                linkQuestionResponse(id, updatedResponseList);
            }

            return true;
        } catch (IllegalAccessException | SQLException e) {
            String errorMsg = String.format("Question Update failed Id:%d" + e.getMessage(), id);
            logger.severe(errorMsg);
            return false;
        }
    }

    /**
     * Removes a Question at the specified Id and all related question_answer Foreign-Keys
     * @param id Primary-Key of the Entry to be removed
     * @return success
     */
    public boolean deleteQuestionById(long id) {
        String questionDeleteQuery = "DELETE FROM question WHERE id = ?";

        try (PreparedStatement questionDeleteStatement = connection.prepareStatement(questionDeleteQuery)) {
            //Remove Question-FK from question_response
            daoResponse.deleteQuestionResponsesForId(id);

            //Delete Question
            questionDeleteStatement.setLong(1, id);
            questionDeleteStatement.execute();

            return true;
        } catch (SQLException e) {
            String errorMsg = String.format("Delete Question failed Id:%d" + e.getMessage(), id);
            logger.severe(errorMsg);
            return false;
        }
    }

    /**
     * Finds a List of all QuestionResponse Objects with a specified topic
     * @param topic Topic-Enum to search on
     * @return List of QuestionResponse Objects matching the topic
     */
    public List<QuestionResponse> searchQuestionByTopic(Topic topic) {
        String query = questionSelectQuery + "WHERE t.topicName LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, topic.toString());
            ResultSet rs = statement.executeQuery();

            return parseResultSet(rs);

        } catch (SQLException | IllegalArgumentException e) {
            String errorMsg = String.format("Topic search failed Topic-Name:%s" + e.getMessage(), topic);
            logger.severe(errorMsg);

            return List.of();
        }
    }

    /**
     * Finds a Question by its Id.
     * @param id The Primary-Key of the Question
     * @return QuestionResponse Object or Empty is the Id was not found.
     */
    public Optional<QuestionResponse> findQuestionById(long id) {
        String query = questionSelectQuery + "WHERE q.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();

            List<QuestionResponse> questions = parseResultSet(rs);
            return questions.isEmpty() ? Optional.empty() : Optional.of(questions.get(0));

        } catch (SQLException | IndexOutOfBoundsException e) {
            String errorMsg = String.format("Question search failed Id:%d" + e.getMessage(), id);
            logger.severe(errorMsg);
        }
        return Optional.empty();
    }

    /**
     * Performs a batch Insert on the question_response table linking a question to several valid responses.
     * @param questionId Primary-Key of the question
     * @param responseIdList List of Primary-Keys for possible responses
     * @throws SQLException If the batch Insert failed.
     */
    private void linkQuestionResponse(long questionId, List<Long> responseIdList) throws SQLException {
        String questionResponseQuery = "INSERT INTO question_response(question_id, response_id) VALUES (?,?)";

        try(PreparedStatement questionResponseInsertStatement = connection.prepareStatement(questionResponseQuery);){
            for (Long responseId : responseIdList) {
                questionResponseInsertStatement.setLong(1, questionId);
                questionResponseInsertStatement.setLong(2, responseId);
                questionResponseInsertStatement.addBatch();
            }
            questionResponseInsertStatement.executeBatch();
        }
    }
}

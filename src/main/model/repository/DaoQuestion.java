package main.model.repository;

import main.model.entity.Question;
import main.model.entity.Response;
import main.model.entity.Topic;
import main.model.entity.dto.QuestionRequest;
import main.model.entity.dto.QuestionResponse;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class DaoQuestion {
    private final String questionSelectQuery = "SELECT q.id as question_id, q.difficultyRankNumber as difficulty, q.content, t.topicName as topic, r.text, r.correct FROM question_response qr JOIN question q ON question_id = q.id JOIN response r ON response_id = r.id JOIN topic t ON topic_id = t.id ";
    private final Connection connection;

    public DaoQuestion(Connection connection) {
        this.connection = connection;
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
            //todo: log this
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
     * Finds the selected Topic and retrieves id
     * @param topic Topic identifier
     * @return Id of the topic or -1 if the topic wasn't found.
     * @throws SQLException If Topic lookup Query failed.
     */
    private int getTopicIdByName(Topic topic) throws SQLException {
        String foreignKeyQuery = "SELECT id AS topic_id FROM topic WHERE topicName LIKE ?";
        try (PreparedStatement topicLookUpStatement = connection.prepareStatement(foreignKeyQuery)) {
            topicLookUpStatement.setString(1, topic.toString());
            ResultSet rs = topicLookUpStatement.executeQuery();

            int topicId = -1;
            if (rs.next()) {
                topicId = rs.getInt("topic_id");
            }
            rs.close();

            return topicId;
        }
    }

    /**
     * Executes a batch insert on the Response table with the List of Response Objects
     * @param responses List of Response Objects.
     * @return List of Ids related  to the saved response Objects.
     * @throws SQLException If the insert failed
     */
    private List<Long> saveResponses(List<Response> responses) throws SQLException {
        String selectQuery = "SELECT id as response_id FROM response WHERE text LIKE ? AND correct = ?";
        String responseInsertQuery = "INSERT IGNORE INTO response(text, correct) VALUES(?,?)";

        try (
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                PreparedStatement responseInsertStatement = connection.prepareStatement(responseInsertQuery, Statement.RETURN_GENERATED_KEYS);
        ) {
            List<Long> responseIdList = new ArrayList<>();

            for (Response response : responses) {
                //Look-up existing Responses
                selectStatement.setString(1, response.text());
                selectStatement.setBoolean(2, response.correct());
                ResultSet result = selectStatement.executeQuery();

                if(result.next()) {
                    responseIdList.add(result.getLong(1));
                    continue;
                }
                result.close();

                //Insert Responses
                responseInsertStatement.setString(1, response.text());
                responseInsertStatement.setBoolean(2, response.correct());
                responseInsertStatement.addBatch();
            }
            responseInsertStatement.executeBatch();

            ResultSet responseKeysResultSet = responseInsertStatement.getGeneratedKeys();
            while (responseKeysResultSet.next()) {
                responseIdList.add(responseKeysResultSet.getLong(1));
            }
            responseKeysResultSet.close();

            return responseIdList;
        }
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

    /**
     * Saves question to the Database
     * Note: This should be wrapped in a Transaction for Production!
     *
     * @return success
     */
    public Long saveQuestion(QuestionRequest request) {
        String questionInsertQuery = "INSERT INTO question(difficultyRankNumber,content,topic_id) VALUES(?,?,?)";

        try (
             PreparedStatement questionInsertStatement = connection.prepareStatement(questionInsertQuery, Statement.RETURN_GENERATED_KEYS);
        ) {

            //Insert Responses
            List<Long> responseIdList = saveResponses(request.responses());

            //Query Topic
            int topicId = getTopicIdByName(request.topic());

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
            //todo: log this!
            return null;
        }
    }


    public boolean updateQuestionById(long id, QuestionRequest newQuestion) {
        String questionUpdateQuery = "UPDATE question SET difficultyRankNumber = ?, content = ?";

        try {
            Optional<QuestionResponse> retrievedQuestion = findQuestionById(id);
            if (retrievedQuestion.isEmpty()) return false;
            QuestionResponse oldQuestion = retrievedQuestion.get();

            List<String> targetFields = findDifference(
                    Question.getQuestion(newQuestion),
                    Question.getQuestion(oldQuestion)
            );

            //Adjust query String based on field to update:
            if (targetFields.contains("topic")) {
                int topicId = getTopicIdByName(newQuestion.topic());
                questionUpdateQuery = questionUpdateQuery.concat(", topic_id = " + topicId);
            }

            PreparedStatement questionUpdateStatement = connection.prepareStatement(questionUpdateQuery + " WHERE id = ?");

            questionUpdateStatement.setInt(1, newQuestion.difficultyRankNumber());
            questionUpdateStatement.setString(2, newQuestion.content());
            questionUpdateStatement.setLong(3, id);
            questionUpdateStatement.executeUpdate();
            questionUpdateStatement.close();

            if (targetFields.contains("responses")) {
                deleteQuestionResponsesForId(id);
                List<Long> updatedResponseList = saveResponses(newQuestion.responses());
                linkQuestionResponse(id, updatedResponseList);
            }

            return true;
        } catch (IllegalAccessException | SQLException e) {
            //todo: log this!
            return false;
        }
    }

    private static List<String> findDifference(Question q1, Question q2) throws IllegalAccessException {

        List<String> differences = new ArrayList<>();
        for (Field field : q1.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value1 = field.get(q1);
            Object value2 = field.get(q2);

            if (!Objects.equals(value1, value2)) {
                differences.add(field.getName());
            }
        }
        return differences;
    }


    private void deleteQuestionResponsesForId(long questionId) throws SQLException {
        String questionResponseQuery = "DELETE FROM question_response WHERE question_id = ?";
        try(PreparedStatement questionResponseStatement = connection.prepareStatement(questionResponseQuery)) {
            questionResponseStatement.setLong(1, questionId);
            questionResponseStatement.execute();
        }
    }
    public boolean deleteQuestionById(long id) {
        String questionDeleteQuery = "DELETE FROM question WHERE id = ?";

        try (PreparedStatement questionDeleteStatement = connection.prepareStatement(questionDeleteQuery)) {
            //Remove Question-FK from question_response
            deleteQuestionResponsesForId(id);

            //Delete Question
            questionDeleteStatement.setLong(1, id);
            questionDeleteStatement.execute();

            return true;
        } catch (SQLException e) {
            //todo: log this!
        }
        return false;
    }

    public List<QuestionResponse> searchQuestionByTopic(Topic topic) {
        String query = questionSelectQuery + "WHERE t.topicName LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, topic.toString());
            ResultSet rs = statement.executeQuery();

            return parseResultSet(rs);

        } catch (SQLException | IllegalArgumentException e) {
            //todo: log this
        }
        return List.of();
    }

    public Optional<QuestionResponse> findQuestionById(long id) {
        String query = questionSelectQuery + "WHERE q.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();

            List<QuestionResponse> questions = parseResultSet(rs);
            return questions.isEmpty() ? Optional.empty() : Optional.of(questions.get(0));

        } catch (SQLException | IndexOutOfBoundsException e) {
            //todo: log this
        }
        return Optional.empty();
    }
}

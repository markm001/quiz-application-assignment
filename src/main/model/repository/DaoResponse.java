package main.model.repository;

import main.model.entity.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoResponse {
    private final Connection connection;

    public DaoResponse(Connection connection) {
        this.connection = connection;
    }

    /**
     * Executes a batch insert on the Response table with the List of Response Objects
     * @param responses List of Response Objects.
     * @return List of Ids related  to the saved response Objects.
     * @throws SQLException If the insert failed
     */
    public List<Long> saveResponses(List<Response> responses) throws SQLException {
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
     * Deletes all question_response table entries for the specified question
     * @param questionId The Primary-Key of the Question
     * @throws SQLException If the deletion failed
     */
    public void deleteQuestionResponsesForId(long questionId) throws SQLException {
        String questionResponseQuery = "DELETE FROM question_response WHERE question_id = ?";
        try(PreparedStatement questionResponseStatement = connection.prepareStatement(questionResponseQuery)) {
            questionResponseStatement.setLong(1, questionId);
            questionResponseStatement.execute();
        }
    }

}

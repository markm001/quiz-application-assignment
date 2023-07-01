package main.model.repository;

import main.model.entity.Topic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DaoTopic {
    private final Connection connection;
    private static final Logger logger = Logger.getLogger(DaoTopic.class.getName());

    public DaoTopic(Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates all specified Topic-Enums in the Database
     * (Debug: Topic.MISSING will be ignored.)
     * @param topics Array of Topics to be created.
     * @return success
     */
    public boolean createTopics(Topic[] topics) {
        String topicInsertQuery = "INSERT IGNORE INTO topic(topicName) VALUES(?)";
        try(PreparedStatement topicInsertStatement = connection.prepareStatement(topicInsertQuery)) {
            for(Topic topic : topics) {
                if(topic.equals(Topic.MISSING)) continue;

                topicInsertStatement.setString(1, topic.name());
                topicInsertStatement.addBatch();
            }
            topicInsertStatement.executeBatch();
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to create the specified topics." + e.getMessage());
            return false;
        }
    }

    /**
     * Finds the selected Topic and retrieves id
     * @param topic Topic identifier
     * @return Id of the topic or -1 if the topic wasn't found.
     * @throws SQLException If Topic lookup Query failed.
     */
    public int getTopicIdByName(Topic topic) throws SQLException {
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
}

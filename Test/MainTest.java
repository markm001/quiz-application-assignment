import main.model.entity.dto.QuestionResponse;
import main.model.repository.DaoQuestion;
import main.util.DatabaseConnector;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    record TestClass(
            int id,
            List<String> content
    ) {}

    record TestClassResponse(
            int id,
            String content
    ) {}

    List<TestClassResponse> x = List.of(
            new TestClassResponse(1,"ab"),
            new TestClassResponse(1,"ab"),
            new TestClassResponse(2,"ab"),
            new TestClassResponse(1,"dz")
    );

    @Test
    void main() {
//        Map<Integer, TestClass> testMap = new HashMap<>();
//        x.forEach(t -> {
//                    List<String> updatedContent = testMap.getOrDefault(t.id, new TestClass(t.id, new ArrayList<>())).content();
//                    updatedContent.add(t.content);
//
//                    testMap.put(t.id, new TestClass(t.id, updatedContent));
//                });
//
//        System.out.println(testMap);

        String x = "Hello World from Test";
        assertEquals("Hello World from Test", x);

//        try(Connection connection = DatabaseConnector.getConnection()) {
//            String query = "SELECT topicName as t FROM topic WHERE id = ?";
//            PreparedStatement statement = connection.prepareStatement(query);
//            statement.setInt(1, 1);
//
//            ResultSet rs = statement.executeQuery();
//
//            if(rs.next()) {
//                System.out.println(rs.getString(1));
//            }
//
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }
}
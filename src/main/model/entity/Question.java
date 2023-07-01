package main.model.entity;

import main.model.entity.dto.QuestionRequest;
import main.model.entity.dto.QuestionResponse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Question {
    private final Topic topic;
    private final int difficultyRankNumber;
    private final String content;
    private final List<Response> responses;

    public Question(Topic topic, int difficultyRankNumber, String content, List<Response> responses) {
        this.topic = topic;
        this.difficultyRankNumber = difficultyRankNumber;
        this.content = content;
        this.responses = responses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return difficultyRankNumber == question.difficultyRankNumber && topic == question.topic && content.equals(question.content) && responses.equals(question.responses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, difficultyRankNumber, content, responses);
    }

    /**
     * Maps QuestionResponse-Object to Question-Object
     * @param q QuestionResponse-Object
     * @return Question-Object
     */
    public static Question getQuestion(QuestionResponse q) {
        return mapToQuestion(
                q.topic(),
                q.difficultyRankNumber(),
                q.content(),
                q.responses()
        );
    }

    /**
     * Maps QuestionRequest-Object to Question-Object
     * @param q QuestionRequest-Object
     * @return Question-Object
     */
    public static Question getQuestion(QuestionRequest q) {
        return mapToQuestion(
                q.topic(),
                q.difficultyRankNumber(),
                q.content(),
                q.responses()
        );
    }

    private static Question mapToQuestion(Topic topic, int rank, String content, List<Response> responses) {
        return new Question(
                topic,
                rank,
                content,
                responses
        );
    }

    /**
     * Compares two QuestionObjects for differences per field
     * @param q1 QuestionObject 1
     * @param q2 QuestionObject 2
     * @return List of Field names where QuestionObjects show difference
     * @throws IllegalAccessException If the field is not accessible
     */
    public static List<String> findDifference(Question q1, Question q2) throws IllegalAccessException {
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
}
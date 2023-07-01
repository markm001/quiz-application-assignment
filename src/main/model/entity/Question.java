package main.model.entity;

import main.model.entity.dto.QuestionRequest;
import main.model.entity.dto.QuestionResponse;

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

    public static Question getQuestion(QuestionResponse q) {
        return mapToQuestion(
                q.topic(),
                q.difficultyRankNumber(),
                q.content(),
                q.responses()
        );
    }
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
}

//package main.model.entity;
//
//import main.model.entity.dto.QuestionRequest;
//import main.model.entity.dto.QuestionResponse;
//
//import java.util.List;
//import java.util.Objects;
//
//public record Question (
//        Topic topic,
//        int difficultyRankNumber,
//        String content,
//        List<Response> responses
//){
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Question question = (Question) o;
//        return difficultyRankNumber == question.difficultyRankNumber && topic == question.topic && content.equals(question.content) && responses.equals(question.responses);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(topic, difficultyRankNumber, content, responses);
//    }
//
//}

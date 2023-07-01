package main.model.entity.dto;

import main.model.entity.Response;
import main.model.entity.Topic;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record QuestionResponse (
        long id,
        Topic topic,
        int difficultyRankNumber,
        String content,
        List<Response> responses
){
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionResponse that = (QuestionResponse) o;
        return id == that.id
                && difficultyRankNumber == that.difficultyRankNumber
                && topic == that.topic && content.equals(that.content)
                && new HashSet<>(responses).containsAll(that.responses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, topic, difficultyRankNumber, content, responses);
    }
}

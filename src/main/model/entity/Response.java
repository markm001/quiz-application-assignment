package main.model.entity;

import java.util.Objects;

public record Response (
        String text,
        boolean correct
){
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return correct == response.correct && text.equals(response.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, correct);
    }
}

package main.model.entity.dto;

import main.model.entity.Response;
import main.model.entity.Topic;

import java.util.List;

public record QuestionRequest (
        Topic topic,
        int difficultyRankNumber,
        String content,
        List<Response> responses
){ }

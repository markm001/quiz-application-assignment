package model.repository;

import main.model.entity.Response;
import main.model.entity.Topic;
import main.model.entity.dto.QuestionRequest;
import main.model.entity.dto.QuestionResponse;
import main.model.repository.DaoQuestion;
import main.util.DatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DaoQuestionTest {
    private static DaoQuestion daoQuestion;

    @BeforeAll
    static void beforeAll() {
        try {
            DatabaseConnector.setAutoCommit(false);
            daoQuestion = new DaoQuestion(DatabaseConnector.getConnection());
        } catch (SQLException ignore) { }
    }

    @AfterEach
    void tearDown() {
        try{
            DatabaseConnector.rollbackChanges();
        } catch (SQLException ignore) { }
    }

    @AfterAll
    static void afterAll() {
        try{
            DatabaseConnector.setAutoCommit(true);
            DatabaseConnector.closeConnection();
        } catch (SQLException ignore) { }
    }

    private List<QuestionRequest> createQuestions(Topic topic, int amount) {
        List<QuestionRequest> topicRequest = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            topicRequest.add(new QuestionRequest(
                    topic,
                    5,
                    "Test"+i,
                    List.of(new Response(topic + "Response" + i, true))
            ));
        }
        return topicRequest;
    }

    private QuestionRequest createQuestion(Topic topic, String responseText) {
        return new QuestionRequest(
                topic,
                5,
                "Test",
                List.of(
                        new Response("text1", true),
                        new Response("text1", false),
                        new Response(responseText, false),
                        new Response("text2", true)
                )
        );
    }

    @Test
    void saveOneQuestion_ReturnOneQuestion() {
        //given
        QuestionRequest request = createQuestion(Topic.FOOD, "Test 1");

        //when
        Long questionId = daoQuestion.saveQuestion(request);

        //then
        assertNotNull(questionId);
        assertEquals(1, daoQuestion.retrieveQuestions().size());
    }

    @Test
    void searchQuestionByExistingTopic_ReturnListOfQuestionsForTopic() {
        //given
        Topic searchTopic = Topic.FOOD;
        int amountForTopic = 5;

        List<QuestionRequest> questionList = new ArrayList<>();
        questionList.addAll(createQuestions(Topic.ARTS, 3));
        questionList.addAll(createQuestions(Topic.CULTURE, 5));
        questionList.addAll(createQuestions(searchTopic, amountForTopic));

        questionList.forEach( q -> daoQuestion.saveQuestion(q) );

        //when
        List<QuestionResponse> questionResponses = daoQuestion.searchQuestionByTopic(searchTopic);

        //then
        assertEquals(amountForTopic, questionResponses.size());
    }
    @Test
    void searchQuestionByMissingTopic_ReturnEmptyListOfQuestions() {
        //given
        Topic excludedTopic = Topic.MISSING;

        List<QuestionRequest> questionList = new ArrayList<>();
        questionList.addAll(createQuestions(Topic.ARTS, 3));
        questionList.addAll(createQuestions(Topic.CULTURE, 5));

        questionList.forEach( q -> daoQuestion.saveQuestion(q) );

        //when
        List<QuestionResponse> questionResponses = daoQuestion.searchQuestionByTopic(excludedTopic);

        //then
        assertEquals(0, questionResponses.size());
    }

    @Test
    void updateQuestionByIdWithNewQuestionRequest_ReturnUpdatedQuestion() {
        //given
        int initialQuestionAmount = 5;
        int questionIndexToUpdate = 0;

        List<QuestionRequest> questionList = new ArrayList<>(
                createQuestions(Topic.ARTS, initialQuestionAmount)
        );

        List<Long> savedQuestionIds = new ArrayList<>();
        questionList.forEach( q -> savedQuestionIds.add(daoQuestion.saveQuestion(q)) );

        assertEquals(savedQuestionIds.size(), initialQuestionAmount);
        Long oldQuestionId = savedQuestionIds.get(questionIndexToUpdate);
        Optional<QuestionResponse> oldQuestion = daoQuestion.findQuestionById(oldQuestionId);

        assertTrue(oldQuestion.isPresent());

        QuestionRequest questionUpdateRequest = new QuestionRequest(
                Topic.FOOD,
                2,
                "Other Content",
                List.of(
                        new Response("Updated Test Response", false)
                )
        );

        //when
        boolean success = daoQuestion.updateQuestionById(oldQuestionId, questionUpdateRequest);

        //then
        assertTrue(success);
        QuestionResponse retrievedQuestionResponse = daoQuestion.findQuestionById(oldQuestionId).get();

        QuestionResponse expectedQuestionResponse = new QuestionResponse(
                oldQuestionId,
                questionUpdateRequest.topic(),
                questionUpdateRequest.difficultyRankNumber(),
                questionUpdateRequest.content(),
                questionUpdateRequest.responses()
        );
        assertEquals(expectedQuestionResponse, retrievedQuestionResponse);
    }

    @Test
    void updateQuestionByIdWithOneExistingResponse_ReturnUpdatedQuestionWithModifiedResponse() {
        //given
        String responseString = "Hello World";
        QuestionRequest question = createQuestion(Topic.ARTS, responseString);
        Long questionId = daoQuestion.saveQuestion(question);

        Optional<QuestionResponse> retrievedQuestion = daoQuestion.findQuestionById(questionId);
        assertTrue(retrievedQuestion.isPresent());

        QuestionResponse oldQuestion = retrievedQuestion.get();

        List<Response> updatedResponses = List.of(
                oldQuestion.responses().get(1),
                oldQuestion.responses().get(2),
                new Response("Updated Test Response", false)
        );

        QuestionRequest questionUpdateRequest = new QuestionRequest(
                oldQuestion.topic(),
                oldQuestion.difficultyRankNumber(),
                oldQuestion.content(),
                updatedResponses
        );

        //when
        boolean success = daoQuestion.updateQuestionById(questionId, questionUpdateRequest);

        //then
        assertTrue(success);
        QuestionResponse retrievedQuestionResponse = daoQuestion.findQuestionById(questionId).get();

        QuestionResponse expectedQuestionResponse = new QuestionResponse(
                questionId,
                questionUpdateRequest.topic(),
                questionUpdateRequest.difficultyRankNumber(),
                questionUpdateRequest.content(),
                questionUpdateRequest.responses()
        );
        assertEquals(expectedQuestionResponse, retrievedQuestionResponse);
    }

    @Test
    void deleteQuestionById_ReturnListMinusOne() {
        //given
        int initialAmount = 10;
        List<QuestionRequest> questionList = new ArrayList<>(
                createQuestions(Topic.ARTS, initialAmount)
        );
        List<Long> savedQuestionIds = new ArrayList<>();
        questionList.forEach( q -> savedQuestionIds.add(daoQuestion.saveQuestion(q)) );

//        Question questionToDelete = daoQuestion.retrieveAllQuestions().get(5);

        //when
        boolean success = daoQuestion.deleteQuestionById(savedQuestionIds.get(1));
//
//        List<Question> filteredList = daoQuestion.retrieveAllQuestions().stream()
//                .filter(q ->
//                        q.equals(questionToDelete)
//                ).toList();

        //then
        assertTrue(success);
//        assertEquals(initialAmount-1, daoQuestion.retrieveAllQuestions().size());
//        assertTrue(filteredList.isEmpty());
    }
}
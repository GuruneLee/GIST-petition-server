package com.gistpetition.api.answer;

import com.gistpetition.api.ServiceTest;
import com.gistpetition.api.answer.application.AnswerService;
import com.gistpetition.api.answer.domain.Answer;
import com.gistpetition.api.answer.domain.AnswerRepository;
import com.gistpetition.api.answer.dto.AnswerRequest;
import com.gistpetition.api.exception.WrappedException;
import com.gistpetition.api.exception.petition.NoSuchPetitionException;
import com.gistpetition.api.exception.petition.UnAnsweredPetitionException;
import com.gistpetition.api.petition.domain.Category;
import com.gistpetition.api.petition.domain.Petition;
import com.gistpetition.api.petition.domain.PetitionRepository;
import com.gistpetition.api.user.domain.User;
import com.gistpetition.api.user.domain.UserRepository;
import com.gistpetition.api.user.domain.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnswerServiceTest extends ServiceTest {

    public static final String ANSWER_CONTENT = "test contents";
    public static final AnswerRequest ANSWER_REQUEST = new AnswerRequest("test contents");
    public static final AnswerRequest UPDATE_REQUEST = new AnswerRequest("change contents");

    @Autowired
    private AnswerService answerService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PetitionRepository petitionRepository;
    @Autowired
    private AnswerRepository answerRepository;

    private User manager;
    private Petition savedPetition;

    @BeforeEach
    void setup() {
        User user = userRepository.save(new User("normal@email.com", "password", UserRole.USER));
        manager = userRepository.save(new User("manager@email.com", "password", UserRole.MANAGER));
        savedPetition = petitionRepository.save(new Petition("title", "description", Category.DORMITORY, user.getId()));
    }

    @Test
    void createAnswerByManager() {
        Long savedAnswer = answerService.createAnswer(savedPetition.getId(), ANSWER_REQUEST, manager.getId());

        Answer answer = answerRepository.findById(savedAnswer).orElseThrow(() -> new WrappedException("존재하지 않는 answer입니다.", null));
        assertThat(answer.getId()).isEqualTo(savedAnswer);
        assertThat(answer.getContent()).isEqualTo(ANSWER_REQUEST.getContent());
        assertThat(answer.getUserId()).isEqualTo(manager.getId());
        assertThat(answer.getPetitionId()).isEqualTo(savedPetition.getId());

        Petition petition = petitionRepository.findById(savedPetition.getId()).orElseThrow(NoSuchPetitionException::new);
        assertTrue(petition.isAnswered());
    }

    @Test
    void createAnswerToNonExistingPetition() {
        Long fakePetitionId = Long.MAX_VALUE;
        assertThatThrownBy(
                () -> answerService.createAnswer(fakePetitionId, ANSWER_REQUEST, manager.getId())
        ).isInstanceOf(NoSuchPetitionException.class);
    }

    @Test
    void retrieveAnswer() {
        Answer saved = answerRepository.save(new Answer(ANSWER_CONTENT, savedPetition.getId(), manager.getId()));

        Answer retrievedAnswer = answerService.retrieveAnswerByPetitionId(savedPetition.getId());

        assertThat(saved.getId()).isEqualTo(retrievedAnswer.getId());
    }

    @Test
    void retrieveAnswerFromNotExistingPetition() {
        assertThatThrownBy(
                () -> answerService.retrieveAnswerByPetitionId(savedPetition.getId())
        ).isInstanceOf(UnAnsweredPetitionException.class);
    }

    @Test
    void retrieveAnswerFromNonExistentPetition() {
        Long notExistingPetitionId = Long.MAX_VALUE;

        assertThatThrownBy(
                () -> answerService.retrieveAnswerByPetitionId(notExistingPetitionId)
        ).isInstanceOf(NoSuchPetitionException.class);
    }

    @Test
    void updateAnswer() {
        Answer answer = answerRepository.save(new Answer(ANSWER_CONTENT, savedPetition.getId(), manager.getId()));
        answerService.updateAnswer(savedPetition.getId(), UPDATE_REQUEST);

        Answer updatedAnswer = answerRepository.findByPetitionId(savedPetition.getId()).orElseThrow(() -> new WrappedException("", null));

        assertThat(answer.getId()).isEqualTo(updatedAnswer.getId());
        assertThat(updatedAnswer.getContent()).isEqualTo(UPDATE_REQUEST.getContent());
    }

    @Test
    void updateAnswerFromNonExistingPetition() {
        Long notExistingPetitionId = Long.MAX_VALUE;

        assertThatThrownBy(
                () -> answerService.updateAnswer(notExistingPetitionId, UPDATE_REQUEST)
        ).isInstanceOf(NoSuchPetitionException.class);
    }

    @Test
    void updateAnswerFromNotAnsweredPetition() {
        assertThatThrownBy(
                () -> answerService.updateAnswer(savedPetition.getId(), UPDATE_REQUEST)
        ).isInstanceOf(UnAnsweredPetitionException.class);
    }

    @Test
    void deleteAnswer() {
        Answer answer = answerRepository.save(new Answer(ANSWER_CONTENT, savedPetition.getId(), manager.getId()));

        answerService.deleteAnswer(savedPetition.getId());

        Petition petition = petitionRepository.findById(savedPetition.getId()).orElseThrow(IllegalArgumentException::new);
        assertFalse(petition.isAnswered());
        assertFalse(answerRepository.existsById(answer.getId()));
    }

    @Test
    void deleteAnswerFromNonExistingPetition() {
        Long notExistingPetitionId = Long.MAX_VALUE;

        assertThatThrownBy(
                () -> answerService.deleteAnswer(notExistingPetitionId)
        ).isInstanceOf(NoSuchPetitionException.class);
    }

    @Test
    void deleteAnswerFromNotAnsweredPetition() {
        assertThatThrownBy(
                () -> answerService.deleteAnswer(savedPetition.getId())
        ).isInstanceOf(UnAnsweredPetitionException.class);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
        answerRepository.deleteAllInBatch();
        petitionRepository.deleteAllInBatch();
    }
}

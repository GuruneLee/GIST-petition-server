package com.example.gistcompetitioncnserver.post;

import com.example.gistcompetitioncnserver.user.User;
import com.example.gistcompetitioncnserver.user.UserRepository;
import com.example.gistcompetitioncnserver.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class PostServiceTest {
    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private AgreementRepository agreementRepository;

    private User user;
    private Long postId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("userName", "email", "password", UserRole.USER));
        postId = postService.createPost(
                new PostRequestDto("title", "description", "category", user.getId()), user.getId());

    }

    @Test
    void agree() {
        Post post = postRepository.findPostByWithEagerMode(postId);
        assertThat(post.getAgreements()).hasSize(0);

        postService.agree(postId, user.getId());
        post = postRepository.findPostByWithEagerMode(postId);
        assertThat(post.getAgreements()).hasSize(1);
    }

    @Test
    void numberOfagreements() {
        User user2 = userRepository.save(new User("userName", "email", "password", UserRole.USER));
        User user3 = userRepository.save(new User("userName", "email", "password", UserRole.USER));

        assertThat(postService.getNumberOfAgreements(postId)).isEqualTo(0);

        postService.agree(postId, user.getId());
        postService.agree(postId, user2.getId());
        postService.agree(postId, user3.getId());

        assertThat(postService.getNumberOfAgreements(postId)).isEqualTo(3);
    }

    @Test
    void getStateOfagreement() {
        assertThat(postService.getStateOfAgreement(postId, user.getId())).isFalse();
        postService.agree(postId, user.getId());
        assertThat(postService.getStateOfAgreement(postId, user.getId())).isTrue();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
        agreementRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
    }
}

package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.handler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactorRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

import java.util.Objects;

@WebFluxTest
//Inject this beans for this particular test class
//Create this beans into spring container so we can use them in this class
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactorRepository reviewReactorRepository;

    @Autowired
    private WebTestClient webTestClient;

    //We are testing just the handler (Unit test) all other things are simulated.
    //Review router is needed as it is part of @Configuration annotation.
    @Test
    void addReview() {
        var review = new Review("1", 1L, "Awesome Movie", 9.0);

        when(reviewReactorRepository.save(isA(Review.class))).thenReturn(Mono.just(review));
        webTestClient.post()
                .uri("/v1/review")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert Objects.nonNull(savedReview);
                    assert Objects.nonNull(savedReview.getReviewId());
                });
    }

    @Test
    void addReviewNullValidation() {
        var review = new Review("1", null, "Awesome Movie", 9.0);

        when(reviewReactorRepository.save(isA(Review.class))).thenReturn(Mono.just(review));
        webTestClient.post()
                .uri("/v1/review")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}

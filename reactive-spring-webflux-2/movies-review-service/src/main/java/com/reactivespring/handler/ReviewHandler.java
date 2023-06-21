package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.repository.ReviewReactorRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
@Slf4j
public class ReviewHandler {
    private final ReviewReactorRepository reviewReactorRepository;
    private final Validator validator;

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .doOnError(throwable -> log.info("Constraint validation error: {}", throwable.getMessage()))
                .flatMap(reviewReactorRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        var constraints = validator.validate(review);
        log.info("constraintsViolations: {}", constraints);
        if (constraints.size() > 0) {
            var errorMessage = constraints
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);

        }

    }

    public Mono<ServerResponse> getAllReviews() {
        var reviewFlux = reviewReactorRepository.findAll();
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> update(ServerRequest request) {

        return Mono.from(reviewReactorRepository.findById(request.pathVariable("id")))
                //Get Mono from result = review
                //Notice we use map within flatMap
                //All inside this flatMap
                .flatMap(review -> request.bodyToMono(Review.class) //Transform request to mono, found mono didn't change
                        //Start changing values from existingMono (review)
                        //reqReview contains request already transformed in mono above
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactorRepository::save)
                        .flatMap(updatedReview -> ServerResponse.ok().bodyValue(updatedReview)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return Mono.from(reviewReactorRepository.findById(request.pathVariable("id")))
                //Return a void so we are going to use then to create a response
                .flatMap(review -> reviewReactorRepository.deleteById(request.pathVariable("id")))
                .then(ServerResponse.noContent().build())
                .switchIfEmpty(ServerResponse.notFound().build());

    }
}


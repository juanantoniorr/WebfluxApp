package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class ReviewHandler {
    private final ReviewReactorRepository reviewReactorRepository;
    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .flatMap(reviewReactorRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
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

    }


package com.reactivespring.client;

import com.reactivespring.domain.Review;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class MoviesReviewRestClient {

    private final  WebClient webClient;
    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public Flux<Review> retrieveReviews(String movieId){
        return webClient.get()
                        .uri(UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                                .queryParam("movieInfoId", movieId)
                                .buildAndExpand().toUriString()).retrieve().bodyToFlux(Review.class);


    }
}

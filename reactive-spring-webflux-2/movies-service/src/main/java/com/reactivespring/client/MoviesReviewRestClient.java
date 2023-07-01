package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsServerException;
import com.reactivespring.util.RetryUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                                .buildAndExpand().toUriString())
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)){
                        return Mono.error(new ReviewsServerException("Reviews server Exception"));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new ReviewsServerException(response)));
                })
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.retry());


    }
}

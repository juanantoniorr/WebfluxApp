package com.reactivespring.controller;

import com.reactivespring.client.MoviesInfoRestClient;
import com.reactivespring.client.MoviesReviewRestClient;
import com.reactivespring.domain.Movie;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private final MoviesInfoRestClient moviesInfoRestClient;
    private final MoviesReviewRestClient moviesReviewRestClient;


    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String id){
       return  moviesInfoRestClient
                .retrieveMovieInfo(id)
                .flatMap(movieInfo -> {
                    //retrieve reviews return a flux we transform it to Mono<List<Review>>
                  var reviewsListMono =  moviesReviewRestClient.retrieveReviews(id).collectList();
                  //We create 1 movie with all the reviews
                 return reviewsListMono.map(reviews -> new Movie(movieInfo,reviews));
                });
    }
}

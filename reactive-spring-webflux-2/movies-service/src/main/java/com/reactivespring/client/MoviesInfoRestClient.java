package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MoviesInfoRestClient {
    private final  WebClient webClient;
    @Value("${restClient.moviesInfoUrl}")
    private  String moviesInfoUrl;

    public Mono<MovieInfo> retrieveMovieInfo(String movieInfoId){
    var url = moviesInfoUrl.concat("/{id}");
    return webClient
            .get()
            .uri(url,movieInfoId)
            .retrieve()
            .bodyToMono(MovieInfo.class)
            .log();

    }
}

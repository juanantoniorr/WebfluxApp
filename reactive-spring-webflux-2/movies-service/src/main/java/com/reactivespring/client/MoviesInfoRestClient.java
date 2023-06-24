package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
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
            .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                log.info("Status code is: {} ", clientResponse.statusCode().value());

                if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)){
                    return Mono.error(
                            new MoviesInfoClientException("There is no MovieInfo available for id: "
                                    + movieInfoId, clientResponse.statusCode().value()));

                }
                return clientResponse.bodyToMono(String.class)
                        .flatMap(response -> Mono.error(new MoviesInfoClientException(response, clientResponse.statusCode().value())));
            })
            .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                log.info("Status code is: {} ", clientResponse.statusCode().value());

                if (clientResponse.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)){
                    return Mono.error(
                            new MoviesInfoServerException("Server exception in MoviesInfoServer: " + clientResponse));

                }
                return clientResponse.bodyToMono(String.class)
                        .flatMap(response -> Mono.error(new MoviesInfoServerException(response)));
            })
            .bodyToMono(MovieInfo.class)
            .log();

    }
}

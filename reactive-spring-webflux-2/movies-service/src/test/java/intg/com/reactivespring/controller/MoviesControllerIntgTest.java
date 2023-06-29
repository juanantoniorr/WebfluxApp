package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("tests")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                //We override the real properties in order to use wiremock instead
               "restClient.moviesInfoUrl= http://localhost:8084/v1/movieinfos",
               "restClient.reviewsUrl= http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void retrieveMovieById(){
        var movieId = "abc";
        //Stub for moviesInfo
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        //Automatically looks for resources/__files
                        .withBodyFile("movieinfo.json")));

        //Stub for reviews
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")

                        //Automatically looks for resources/__files
                        .withBodyFile("reviews.json")));


        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size()==2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }

    @Test
    void retrieveMovieById_404_moviesInfo(){
        var movieId = "abc";
        //Stub for moviesInfo
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_NOT_FOUND)));

        //Stub for reviews
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")

                        //Automatically looks for resources/__files
                        .withBodyFile("reviews.json")));


        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo available for id: " + movieId);
        WireMock.verify(1,getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));

    }

    @Test
    void retrieveMovieById_404_reviews(){
        var movieId = "abc";
        //Stub for moviesInfo
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        //Automatically looks for resources/__files
                        .withBodyFile("movieinfo.json")));

        //Stub for reviews
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_NOT_FOUND)));

//ToDo fix reviews controller to retrieve an empty mono when there are no reviews
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                //Will be ok but reviewsList will be empty
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size()==0;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });


    }

    @Test
    void retrieveMovieById_500_moviesInfo(){
        var movieId = "abc";
        //Stub for moviesInfo
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)));

        //Stub for reviews
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")

                        //Automatically looks for resources/__files
                        .withBodyFile("reviews.json")));


        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in MoviesInfoServer");

        WireMock.verify(4,getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));
    }

@Test
    void retrieveMovieById_500_reviews(){
        var movieId = "abc";
    stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    //Automatically looks for resources/__files
                    .withBodyFile("movieinfo.json")));

    //Stub for reviews
    stubFor(get(urlPathEqualTo("/v1/reviews"))
            .willReturn(aResponse()
                    .withStatus(500)));


    //ToDo return ReviewsServerException instead of internal server error
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class);

        WireMock.verify(4,getRequestedFor(urlPathMatching("/v1/reviews*")));
    }

}

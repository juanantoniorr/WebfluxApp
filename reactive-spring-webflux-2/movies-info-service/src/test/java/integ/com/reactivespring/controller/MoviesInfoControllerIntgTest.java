package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfos)
                //Without this the methods are running async so maybe test cases
                //will run before the data is persisted
                //Blocking calls are only accepted in test cases
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll();
    }

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        webTestClient.post()
                .uri("/v1/movieinfos")
                .bodyValue(movieInfo)
                //Make call to the endpoint
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void getAllMovieInfo() {
        webTestClient.get()
                .uri("/v1/movieinfos")
                //Make call to the endpoint
                .exchange()
                .expectStatus()
                .isFound()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);

    }

    @Test
    void updateMovieInfo() {
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        var infoId = "abc";

        webTestClient.put()
                .uri("/v1/movieinfos/{id}", infoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                var updatedInfo = movieInfoEntityExchangeResult.getResponseBody();
                assert Objects.nonNull(updatedInfo);
                assert Objects.nonNull(updatedInfo.getMovieInfoId());
                });
    }

    @Test
    void updateMovieInfoNotFound() {
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        var infoId = "abcd";

        webTestClient.put()
                .uri("/v1/movieinfos/{id}", infoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getAllMovieInfo_stream() {
        //given
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        webTestClient.post()
                .uri("/v1/movieinfos")
                .bodyValue(movieInfo)
                //Make call to the endpoint
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });

        var moviesStreamFlux = webTestClient.get()
                .uri("/v1/movieinfos/stream")
                //Make call to the endpoint
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        StepVerifier.create(moviesStreamFlux)
                .assertNext(movieInfo1 -> {
                    assert Objects.nonNull(movieInfo1);

                })
                .thenCancel()
                .verify();


    }


    }

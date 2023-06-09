package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//Spring looks for all repository classes and start them for IT (just repos not the whole app)
@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntTest {
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
    void findAll() {
        var moviesInfoFlux = movieInfoRepository.findAll().log();
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void findById() {
        var moviesInfoMono = movieInfoRepository.findById("abc");
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> assertEquals("Dark Knight Rises", movieInfo.getName()))
                .verifyComplete();
    }
    @Test
    void save() {
        var moviesInfoMono = movieInfoRepository.save(new MovieInfo(null, "Test",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")));
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Test", movieInfo.getName());
                })
                .verifyComplete();
    }
}
package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class MovieInfoService {
    private final MovieInfoRepository movieInfoRepository;

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
    return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> findAll() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> update(MovieInfo updatedMovieInfo, String id){
        return movieInfoRepository.findById(id)
                . flatMap(movieInfo -> {
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setRelease_day(updatedMovieInfo.getRelease_day());
                    movieInfo.setYear(updatedMovieInfo.getYear());
                    return movieInfoRepository.save(movieInfo);
                });

    }

    public Mono<MovieInfo> findById(String id) {
        return movieInfoRepository.findById(id)
                .switchIfEmpty(Mono.empty());
    }
}

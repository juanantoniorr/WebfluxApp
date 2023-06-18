package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {
    @Bean
    public RouterFunction<ServerResponse> reviewRoute(ReviewHandler reviewHandler){
        return route()
                .nest(path("/v1/review"), builder -> {
                    builder.GET("/helloworld", (request -> ServerResponse.ok().bodyValue("Hello world")))
                            .GET("",request -> reviewHandler.getAllReviews())
                            .POST("", request -> reviewHandler.addReview(request))
                            .PUT("/{id}", request -> reviewHandler.update(request));

                })

                .build();
    }

}

package com.reactivespring.util;

import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryUtil {

    public static Retry retry(){
       return Retry.fixedDelay(3, Duration.ofSeconds(1))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        Exceptions.propagate(retrySignal.failure()))
                //We just retry on server exception for client exception is not worth it
                .filter(ex -> ex instanceof MoviesInfoServerException || ex instanceof ReviewsServerException);
    }
}

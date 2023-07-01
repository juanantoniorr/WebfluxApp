package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

public class SinksTest {
    @Test
    void testSink() {
        //replay with resend previous events to new subscribers and then emit new data.
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

        var integerFlux = replaySink.asFlux();
        integerFlux.subscribe( integer -> {
            System.out.println("Subscriber 1 : " + integer);
        });

        var integerFlux2 = replaySink.asFlux();
        integerFlux2.subscribe( integer -> {
            System.out.println("Subscriber 2 : " + integer);
        });

        var integerFlux3 = replaySink.asFlux();
        integerFlux3.subscribe( integer -> {
            System.out.println("Subscriber 3 : " + integer);
        });

        //This event will be repeated for all subscribers
        replaySink.tryEmitNext(4);

    }

    @Test
    public void sink_multicast (){
        //New subscribers will just get new data no previous data
        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();
        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

        var integerFlux = multicast.asFlux();
        integerFlux.subscribe( integer -> {
            System.out.println("Subscriber 1 : " + integer);
        });

        multicast.tryEmitNext(4);

        //This second subscriber just get {5} no previous data
        var integerFlux2 = multicast.asFlux();
        integerFlux2.subscribe( integer -> {
            System.out.println("Subscriber 2 : " + integer);
        });
        multicast.tryEmitNext(5);



    }
}

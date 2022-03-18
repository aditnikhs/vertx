package com.vertx.demo;

import io.vertx.core.AbstractVerticle;
import java.lang.String;
import java.util.UUID;

public class HelloVerticle extends AbstractVerticle {
    
    String verticleId = UUID.randomUUID().toString();

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("hello.vertx.addr", (msg) -> {
            msg.reply("Hello Vert.x World!");
        } );

        vertx.eventBus().consumer("hello.name.addr", (msg) -> {
            String name = (String)msg.body();
            msg.reply(String.format("Hello $s, from $s !",name, verticleId)); 
        } );
    }
}

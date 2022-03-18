package com.vertx.demo;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.lang.Integer;
import com.vertx.demo.*;

public class MainVerticle extends AbstractVerticle {
 
    @Override
    public void start(Promise<Void> start) {
        vertx.deployVerticle(new HelloVerticle());
        Router router = Router.router(vertx);
        router.route().handler(context -> {
            String authToken = context.request().getHeader("AUTH_TOKEN");
            if(authToken != null && "mySuperSecretAuthToken".contentEquals(authToken)){
                context.next();
            }else{
                context.response().setStatusCode(401).setStatusMessage("UNAUTHORIZED").end();
            }
        });
        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloName);
        router.route().handler(StaticHandler.create("web"));

        ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path","config.json"));
            
        ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
            .addStore(defaultConfig);

        ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);

        Handler<AsyncResult<JsonObject>> handler = asyncResult -> this.handleConfigResult(start, router, asyncResult);
        cfgRetriever.getConfig(handler);

        
    }

    void handleConfigResult(Promise<Void> start, Router router, AsyncResult<JsonObject> asyncResult){
        if(asyncResult.succeeded()){
            JsonObject config = asyncResult.result();
            JsonObject http = config.getJsonObject("http");
            int httpPort = http.getInteger("port");
            vertx.createHttpServer().requestHandler(router).listen(httpPort);
            start.complete();
        }else{
            start.fail("Unable to load configuration.");
        }
    }

    void helloVertx(RoutingContext context){
        vertx.eventBus().request("hello.vertx.addr", "", reply  -> {
            context.request().response().end((String)reply.result().body());
        });
    }

    void helloName(RoutingContext context){
        String name = context.pathParam("name");
        vertx.eventBus().request("hello.name.addr", name, reply  -> {
            context.request().response().end((String)reply.result().body());
        });
    }

}

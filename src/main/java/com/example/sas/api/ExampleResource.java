package com.example.sas.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/quarkus-hello")
public class ExampleResource {
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(@HeaderParam("x-skyott-usertoken") String token) {
        return Response.ok("Hello from REST Client Reactive - Vertx").build();
    }
}
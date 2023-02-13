package com.example.sas.gateway;

import io.quarkus.runtime.util.StringUtil;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Provider
@ApplicationScoped
@RegisterRestClient
@RegisterClientHeaders
public class RequestFilters {

    @Inject
    Logger logger;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private static final String USER_TOKEN_UMV = "x-skyott-usertoken";

    @ServerRequestFilter
    public Response requestFilter(ContainerRequestContext requestContext) throws Exception {

        if (StringUtil.isNullOrEmpty(requestContext.getHeaders().getFirst(USER_TOKEN_UMV))) {
            logger.error("UMV token is missing.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        logger.info("Base URI >> " + requestContext.getUriInfo().getBaseUri().toString() + 
                " and path is " + requestContext.getUriInfo().getBaseUri().getPath());
        logger.info("Absolute Path >> " + requestContext.getUriInfo().getAbsolutePath().toString() + 
                " and path is " + requestContext.getUriInfo().getAbsolutePath().getPath());

        //proceed with the request
        var requestUri = requestContext.getUriInfo().getRequestUri().getPath();
        logger.info("Request URI is " + requestUri);

        this.httpClient
                .sendAsync(HttpRequest.newBuilder()
                                .method(requestContext.getMethod(),
                                        HttpRequest.BodyPublishers.ofString(requestContext.getEntityStream().toString()))
                                //.uri(URI.create("http://localhost:8081/quarkus-hello"))
                                .uri(requestContext.getUriInfo().getAbsolutePath())
                                .header("x-skyott-usertoken", "sometoken")
                                .header("hosueholdId", "3r34344q1fqf4")
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                ).thenApply(HttpResponse::body)
                .toCompletableFuture();

        return Response.ok().build();
    }
}

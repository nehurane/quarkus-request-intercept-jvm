package com.example.sas.gateway;

import io.quarkus.runtime.util.StringUtil;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class RequestFilters {

    @Inject
    UriInfo uriInfo;

    @Inject
    Logger logger;

    private final Vertx vertx;
    private final WebClient client;

    @Inject
    public RequestFilters(Vertx vertx, WebClient client) {
        this.vertx = vertx;
        this.client = WebClient.create(vertx);
    }

    private static final String USER_TOKEN_UMV = "x-skyott-usertoken";

    @ServerRequestFilter
    public void requestFilter(ContainerRequestContext requestContext) throws Exception {

        logger.info("Check AUTH Filter --> Request Context " + requestContext.toString());

        if (StringUtil.isNullOrEmpty(requestContext.getHeaders().getFirst(USER_TOKEN_UMV))) {
            logger.error("UMV token is missing.");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        logger.info("Base URI >> " + uriInfo.getBaseUri().toString() + " and base URI path is " + uriInfo.getBaseUri().getPath());
        logger.info("Absolute Path >> " + uriInfo.getAbsolutePath().toString() + " and absolute path is " + uriInfo.getAbsolutePath().getPath());

        //proceed with the request
        var requestUri = uriInfo.getRequestUri().getPath();
        logger.info("Request URI is " + requestUri);

        client.getAbs(requestUri)
                //.host("")
                //.port(8080)
                .send();
    }
}

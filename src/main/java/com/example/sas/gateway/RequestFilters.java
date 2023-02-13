package com.example.sas.gateway;

import io.quarkus.runtime.util.StringUtil;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@ApplicationScoped
@Provider
@RegisterRestClient(configKey = "sas-api")
@RegisterClientHeaders
public class RequestFilters {
    @Inject
    Logger logger;

    private final WebClient client;

    @Inject
    RequestFilters(Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    private static final String USER_TOKEN_UMV = "x-skyott-usertoken";

    @ServerRequestFilter
    public Response requestFilter(ContainerRequestContext requestContext) throws Exception {

        logger.info("Check AUTH Filter --> Request Context " + requestContext.toString());

        if (StringUtil.isNullOrEmpty(requestContext.getHeaders().getFirst(USER_TOKEN_UMV))) {
            logger.error("UMV token is missing.");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        logger.info("Base URI >> " + requestContext.getUriInfo().getBaseUri().toString() + " " +
                "and URI path is " + requestContext.getUriInfo().getBaseUri().getPath());
        logger.info("Absolute Path >> " + requestContext.getUriInfo().getAbsolutePath().toString() + " " +
                "and URIpath is " + requestContext.getUriInfo().getAbsolutePath().getPath());

        client.getAbs(requestContext.getUriInfo().getAbsolutePath().toString())
                .putHeader(USER_TOKEN_UMV, "q3445334235425345")
                .putHeader("householdid", "ABCBOJE232134")
                .method(HttpMethod.valueOf(requestContext.getMethod()))
                //.host("")
                //.port(8080)
                .send();
        
        return Response.ok().build();
    }
}

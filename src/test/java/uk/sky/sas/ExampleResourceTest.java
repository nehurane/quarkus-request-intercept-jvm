package uk.sky.sas;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ExampleResourceTest {

    @Test
    void testHelloEndpointWithoutToken() {
        given()
                .when()
                .get("/quarkus-hello")
                .then()
                .statusCode(401);
    }

    @Test
    void testHelloEndpointWithToken() {
        given()
                .when()
                .header("x-skyott-usertoken", "qffn9a8efa9f8e98e")
                .get("/quarkus-hello")
                .then()
                .statusCode(200);
    }

}
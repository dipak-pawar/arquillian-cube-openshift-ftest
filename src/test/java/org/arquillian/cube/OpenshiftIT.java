package org.arquillian.cube;

import io.restassured.RestAssured;
import java.net.URL;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;


@RunWith(Arquillian.class)
public class OpenshiftIT {

    @AwaitRoute
    @RouteURL("${app.name}")
    private URL baseURL;

    @Before
    public void setup() throws Exception {
        RestAssured.baseURI = baseURL.toString();
    }

    @Test
    public void testGreetingEndpoint() {
            when()
                .get()
            .then()
                .statusCode(200)
                .body(containsString("Greetings from Spring Boot!"));
    }
}

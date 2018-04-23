## Cube way of writing Booster tests !

This guide introduces you to Arquillian Cube. After reading this guide, you’ll be able to:

-   Write an Arquillian test which deploys your application on openshift cluster and verify your application.
-   Execute an Integration Test in both Maven and IDE using arquillian configuration.

You'll learn how to write an integration test for Openshift using Arquillian Cube and it's configuration.

### Assumptions

This guide assumes you have openshift cluster up and running. You should be logged in as `developer` user. If you don't please download and install [minishift](https://docs.openshift.org/latest/minishift/getting-started/installing.html). Also make sure you have Maven available, either in your command shell or your IDE (Integrated Development Environment). If you don't, please [download and install Maven now](http://maven.apache.org/download.html). You'll also need [JDK (Java Development Kit) 1.8](http://www.oracle.com/technetwork/java/javase/downloads) installed on your machine.

### Application Under Testing

In order to write an Arquillian Cube Integration Test, you need to have a application for it. Let's take a clone of sample [spring boot application](https://github.com/dipak-pawar/arquillian-cube-openshift-ftest) which we are going to use in this guide.

```bash
git clone https://github.com/dipak-pawar/arquillian-cube-openshift-ftest
```

### Setting Dependencies and Plugin Configurations

Go ahead and open up the `pom.xml` in your editor. Add following dependencies in pom.xml inside `<dependencies>` section.

```xml
<dependencies>
  <dependency>
    <groupId>org.arquillian.cube</groupId>
    <artifactId>arquillian-cube-openshift-starter</artifactId> <!-- #<1> -->
    <version>${version.arquillian.cube}</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId> <!-- #<2> -->
    <version>${version.restassured}</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId> <!-- #<3> -->
    <version>${version.junit}</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

<1> Cube openshift dependency for standalone mode.

<2> Validate interesting things from response.

<3> Unit Testing Framework.

Add `Maven` profile with [fabric8-maven-plugin](https://maven.fabric8.io/) configuration as follows:

```xml
<profiles>
  <profile>
  <id>openshift</id>
    <build>
      <plugins>
        <plugin>
          <groupId>io.fabric8</groupId>
          <artifactId>fabric8-maven-plugin</artifactId>
          <version>${version.fabric8.maven.plugin}</version>
          <executions>
            <execution>
              <goals>
                <goal>resource</goal>
                <goal>build</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```
Add [Failsafe Plugin](http://maven.apache.org/surefire/maven-failsafe-plugin) and it's configuration to run integration tests.

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <version>${version.failsafe.plugin}</version>
  <executions>
    <execution>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
     </execution>
  </executions>
</plugin>
```

You can find entire `pom.xml` [here](https://raw.githubusercontent.com/dipak-pawar/arquillian-cube-openshift-ftest/integration_test/pom.xml).

### Write Integration Test

Once all dependency and plugin configuration is set, you are ready to write Integration Test. In your IDE, create a new Java class named `OpenshiftIT` in the `org.arquillian.cube` package. Add following code in it.

```java
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
  @RouteURL("${app.name}") // #<1>
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
```
<1> To Resolve `${app.name}`, you have to set `app.name` either using system property or environment variable or arquillian.xml properties.

### Creating Arquillian Configuration for Integration Test

Below snippet shows how to set Arquillian configuration for your integration test which is added in `src/test/resources/arquillian.xml`

```xml
<arquillian xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns="http://jboss.org/schema/arquillian"
  xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd">

  <extension qualifier="openshift">
    <property name="namespace.use.current">true</property> <!-- #<1> -->
    <property name="env.init.enabled">true</property> <!-- #<2> -->
    <property name="enableImageStreamDetection">false</property> <!-- #<3> -->

    <!--To resolve @RouteUrl expression -->
    <property name="app.name">arquillian-cube-openshift-ftest</property> <!-- #<4> -->

    <!-- To execute fmp goal 'mvn package fabric8:build fabric8:resource' in test execution. -->
    <property name="cube.fmp.build.disable.for.mvn">true</property> <!-- #<5> -->
    <property name="cube.fmp.profiles">openshift</property> <!-- #<6> -->
  </extension>

</arquillian>
```
<1> Use current namespace for this test.

<2> Initialize environment (apply kubernetes resources).

<3> Disable detecting ImageStream resources located at `target/*-is.json`.

<4> Resolve `@RouteUrl` expression used in injecting baseUrl for your application.

<5> Execute fabric8-maven-plugin goal `mvn package fabric8:build fabric8:resource -Dfabric8.namespace=${namespace_configured_to_use_in_test}` during test execution from `IDE`. Don't execue it while running from `Maven`.

<6> Enable profile with `fabric8-maven-plugin` configuration.

### Run Integration Test

1. To Run integration test from Maven

```bash
mvn clean install -Popenshift
```

2. To run the same test from `IDE`, simply select `Run as JUnit Test` option.

### What's Next

You can find full source code with application and integration test [here](https://github.com/dipak-pawar/arquillian-cube-openshift-ftest/tree/integration_test). Similarly, you can write integration tests for `Vert.x`, `Wildfly Swarm` applications.

You can find more examples [here](https://github.com/arquillian/arquillian-cube/tree/master/openshift).

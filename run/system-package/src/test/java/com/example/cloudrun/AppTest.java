package com.example.cloudrun;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.utils.IOUtils;

public class AppTest {

  String BASE_URL = "/diagram.png";
  String DOT = "?dot=";

  @BeforeClass
  public static void beforeClass() {
    App app = new App();
    app.main(new String[] {});
    awaitInitialization();
  }

  @AfterClass
  public static void afterClass() {
    stop();
  }

  @Test
  public void shouldFailWithNoQuery() {
    try {
      TestResponse response = executeRequest("GET", BASE_URL);
      assertEquals(true, response);
    } catch (IOException e) {
      assertTrue(e.getMessage().startsWith("Server returned HTTP response code: 400 for URL"));
    }
  }

  @Test
  public void shouldFailWithEmptyDotParam() {
    try {
      executeRequest("GET", BASE_URL + DOT);
    } catch (IOException e) {
      assertTrue(e.getMessage().startsWith("Server returned HTTP response code: 400 for URL"));
    }
  }

  @Test
  public void shouldFailWithInvalidPayload() {
    try {
      executeRequest("GET", BASE_URL + DOT + "digraph");
    } catch (IOException e) {
      assertTrue(e.getMessage().startsWith("Server returned HTTP response code: 400 for URL"));
    }
  }

  @Test
  public void shouldSucceed() throws IOException {
    String query = "digraph%20G%20{%20A%20->%20{B,%20C,%20D}%20->%20{F}%20}";
    TestResponse response = executeRequest("GET", BASE_URL + DOT + query);
    System.out.println(response);
    assertEquals(200, response.status);
  }

  private static TestResponse executeRequest(String method, String path) throws IOException {
    URL url = new URL("http://localhost:8080" + path);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setDoOutput(true);
    connection.connect();
    String body = IOUtils.toString(connection.getInputStream());
    return new TestResponse(connection.getResponseCode(), body);
  }

  public static class TestResponse {

    public final String body;
    public final int status;

    public TestResponse(int status, String body) {
      this.status = status;
      this.body = body;
    }
  }
}

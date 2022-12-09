/*
 * Copyright 2022 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.api.apikeys.v2.Key;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ApiKeySnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String CREDENTIALS = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
  private static Key API_KEY;
  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    API_KEY = CreateApiKey.createApiKey(PROJECT_ID);

    stdOut.close();
    System.setOut(out);
  }

  @AfterClass
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    String apiKeyId = API_KEY.getName().split("/")[5];
    DeleteApiKey.deleteApiKey(PROJECT_ID, apiKeyId);
    assertThat(stdOut.toString()).contains(
        String.format("Successfully deleted the API key: %s", API_KEY.getName()));

    stdOut.close();
    System.setOut(out);
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testLookupApiKey() throws IOException {
    LookupApiKey.lookupApiKey(API_KEY.getKeyString());
    assertThat(stdOut.toString()).contains(
        String.format("Successfully retrieved the API key name: %s", API_KEY.getName()));
  }

  @Test
  public void testRestrictApiKeyAndroid()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    RestrictApiKeyAndroid.restrictApiKeyAndroid(PROJECT_ID, API_KEY.getName());
    assertThat(stdOut.toString()).contains(
        String.format("Successfully updated the API key: %s", API_KEY.getName()));
  }

  @Test
  public void testRestrictApiKeyApi()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    RestrictApiKeyApi.restrictApiKeyApi(PROJECT_ID, API_KEY.getName());
    assertThat(stdOut.toString()).contains(
        String.format("Successfully updated the API key: %s", API_KEY.getName()));
  }

  @Test
  public void testRestrictApiKeyHttp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    RestrictApiKeyHttp.restrictApiKeyHttp(PROJECT_ID, API_KEY.getName());
    assertThat(stdOut.toString()).contains(
        String.format("Successfully updated the API key: %s", API_KEY.getName()));
  }

  @Test
  public void testRestrictApiKeyIos()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    RestrictApiKeyIos.restrictApiKeyIos(PROJECT_ID, API_KEY.getName());
    assertThat(stdOut.toString()).contains(
        String.format("Successfully updated the API key: %s", API_KEY.getName()));
  }

  @Test
  public void testRestrictApiKeyServer()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    RestrictApiKeyServer.restrictApiKeyServer(PROJECT_ID, API_KEY.getName());
    assertThat(stdOut.toString()).contains(
        String.format("Successfully updated the API key: %s", API_KEY.getName()));
  }
}
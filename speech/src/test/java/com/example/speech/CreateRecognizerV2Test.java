/*
 * Copyright 2023 Google LLC
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

package com.example.speech;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v2.DeleteRecognizerRequest;
import com.google.cloud.speech.v2.OperationMetadata;
import com.google.cloud.speech.v2.Recognizer;
import com.google.cloud.speech.v2.RecognizerName;
import com.google.cloud.speech.v2.SpeechClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class CreateRecognizerV2Test {
  private String recognizerId = String.format("rec-%s", UUID.randomUUID());
  private String recognizerName;
  private String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() throws IOException, ExecutionException, InterruptedException,
      TimeoutException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    recognizerName = RecognizerName.format(projectId, "global", recognizerId);
  }

  @After
  public void tearDown() throws IOException, ExecutionException, InterruptedException,
      TimeoutException {
    System.setOut(out);

    DeleteRecognizerRequest deleteRequest = DeleteRecognizerRequest.newBuilder()
        .setName(recognizerName)
        .build();

    try (SpeechClient speechClient = SpeechClient.create()) {
      OperationFuture<Recognizer, OperationMetadata> op =
          speechClient.deleteRecognizerAsync(deleteRequest);
      op.get(180, TimeUnit.SECONDS);
    }
  }

  @Test
  public void testQuickstart() throws Exception {
    // Act
    CreateRecognizerV2.createRecognizerV2(projectId, recognizerId);

    // Assert
    String got = bout.toString();
    assertThat(got).contains(recognizerId);
  }
}

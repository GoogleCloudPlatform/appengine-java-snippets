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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.DeleteCustomClassRequest;
import com.google.cloud.speech.v1.DeletePhraseSetRequest;
import com.google.cloud.speech.v2.CreateRecognizerRequest;
import com.google.cloud.speech.v2.CustomClass;
import com.google.cloud.speech.v2.CustomClassName;
import com.google.cloud.speech.v2.DeleteRecognizerRequest;
import com.google.cloud.speech.v2.OperationMetadata;
import com.google.cloud.speech.v2.PhraseSet;
import com.google.cloud.speech.v2.PhraseSetName;
import com.google.cloud.speech.v2.Recognizer;
import com.google.cloud.speech.v2.SpeechClient;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class AdaptationCustomClassReferenceV2Test {
  private String recognitionAudioFile = "./resources/commercial_mono.wav";
  private String recognizerId = String.format("rec-%s", UUID.randomUUID());
  private String customClassId = String.format("cls-%s", UUID.randomUUID());
  private String phraseSetId = String.format("phrase-%s", UUID.randomUUID());
  private String recognizerName;
  private String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() throws InterruptedException, ExecutionException, TimeoutException, IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // Create a recognizer for this test.
    try (SpeechClient speechClient = SpeechClient.create()) {
      String parent = String.format("projects/%s/locations/global", projectId);

      // First, create a recognizer
      Recognizer recognizer = Recognizer.newBuilder()
          .setModel("latest_long")
          .addLanguageCodes("en-US")
          .build();

      CreateRecognizerRequest createRecognizerRequest = CreateRecognizerRequest.newBuilder()
          .setParent(parent)
          .setRecognizerId(recognizerId)
          .setRecognizer(recognizer)
          .build();

      OperationFuture<Recognizer, OperationMetadata> op =
          speechClient.createRecognizerAsync(createRecognizerRequest);
      recognizer = op.get(180, TimeUnit.SECONDS);
      recognizerName = recognizer.getName();
    }
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

      OperationFuture<PhraseSet, OperationMetadata> deletePhraseOp =
          speechClient.deletePhraseSetAsync(PhraseSetName.format(projectId, "global", phraseSetId));
      deletePhraseOp.get(180, TimeUnit.SECONDS);
      
      OperationFuture<CustomClass, OperationMetadata> deleteClassOp =
          speechClient.deleteCustomClassAsync(CustomClassName.format(projectId, "global", customClassId));
      deleteClassOp.get(180, TimeUnit.SECONDS);      

    }
  }
}

/*
 * Copyright 2024 Google LLC
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

package aiplatform;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;
import com.google.api.gax.rpc.ApiException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatchCodePredictionTest {

  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String LOCATION = "us-central1";
  private static String BUCKET ;
  private static String OPERATION;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;


  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("UCAIP_PROJECT_ID");

  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
//    // Delete the created data labeling
//    DeleteDataLabelingJobSample.deleteDataLabelingJob(PROJECT, dataLabelingJobId);

//    // Assert
//    String deleteResponse = bout.toString();
//    assertThat(deleteResponse).contains("Deleted Data Labeling Job.");
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  @SuppressWarnings("checkstyle:abbreviationaswordinname")
  public void testBatchCodePrediction() throws IOException, InterruptedException {
    try {
      String gcsSourceUri =
          "gs://cloud-samples-data/batch/prompt_for_batch_code_predict.jsonl";
      String gcsDestinationOutputUriPrefix =
          String.format("gs://%s/ucaip-test-output/", PROJECT);
      String modelId = "code-bison";

      BatchCodePrediction.batchCodePrediction(
          PROJECT, LOCATION, gcsSourceUri, gcsDestinationOutputUriPrefix, modelId);
      OPERATION = "YOUR_OPERATION_ID";
      assertThat(OPERATION).isNotNull();
    } catch (ApiException ex) {
      System.out.println(ex.getMessage());
      // Example of expected exeption:
      assertThat(ex.getMessage())
          .contains("The model projects/ucaip-sample-tests/locations/us-central1/publishers/google/models/text-bison@001 is not available for online prediction.");
    }
  }
}

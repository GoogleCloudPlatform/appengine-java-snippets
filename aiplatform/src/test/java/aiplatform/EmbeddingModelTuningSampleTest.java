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
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.aiplatform.v1.CancelPipelineJobRequest;
import com.google.cloud.aiplatform.v1.DeleteOperationMetadata;
import com.google.cloud.aiplatform.v1.PipelineJob;
import com.google.cloud.aiplatform.v1.PipelineServiceClient;
import com.google.cloud.aiplatform.v1.PipelineServiceSettings;
import com.google.cloud.aiplatform.v1.PipelineState;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.protobuf.Empty;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedRunnable;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EmbeddingModelTuningSampleTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String API_ENDPOINT = "us-central1-aiplatform.googleapis.com:443";
  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String BASE_MODEL_VERSION_ID = "textembedding-gecko@003";
  private static final String TASK_TYPE = "DEFAULT";
  private static final String QUERIES =
      "gs://embedding-customization-pipeline/dataset/queries.jsonl";
  private static final String CORPUS = "gs://embedding-customization-pipeline/dataset/corpus.jsonl";
  private static final String TRAIN_LABEL =
      "gs://embedding-customization-pipeline/dataset/train.tsv";
  private static final String TEST_LABEL = "gs://embedding-customization-pipeline/dataset/test.tsv";
  private static final String OUTPUT_DIR =
      "gs://ucaip-samples-us-central1/training_pipeline_output";
  private static final int BATCH_SIZE = 50;
  private static final int ITERATIONS = 300;
  private static Queue<String> pipelineJobNames = new LinkedList<String>();

  private static final RetryConfig RETRY_CONFIG =
      RetryConfig.custom()
          .maxAttempts(10)
          .waitDuration(Duration.ofSeconds(10))
          .retryExceptions(TimeoutException.class)
          .failAfterMaxAttempts(false)
          .build();
  private static final RetryRegistry RETRY_REGISTRY = RetryRegistry.of(RETRY_CONFIG);

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

  @AfterClass
  public static void tearDown() throws Throwable {
    PipelineServiceSettings settings =
        PipelineServiceSettings.newBuilder().setEndpoint(API_ENDPOINT).build();
    try (PipelineServiceClient client = PipelineServiceClient.create(settings)) {
      List<CancelPipelineJobRequest> reqs =
          pipelineJobNames.stream()
              .map(n -> CancelPipelineJobRequest.newBuilder().setName(n).build())
              .collect(toList());
      CheckedRunnable runnable =
          Retry.decorateCheckedRunnable(
              RETRY_REGISTRY.retry("delete-pipeline-jobs", RETRY_CONFIG),
              () -> {
                List<OperationFuture<Empty, DeleteOperationMetadata>> deletions =
                    reqs.stream()
                        .map(
                            req -> {
                              client.cancelPipelineJobCallable().futureCall(req);
                              return client.deletePipelineJobAsync(req.getName());
                            })
                        .collect(toList());
                for (OperationFuture<Empty, DeleteOperationMetadata> d : deletions) {
                  d.get(0, TimeUnit.SECONDS);
                }
              });
      try {
        runnable.run();
      } catch (TimeoutException e) {
        // Do nothing.
      }
    }
  }

  @Test
  public void createPipelineJobEmbeddingModelTuningSample() throws IOException {
    PipelineJob pipelineJob =
        EmbeddingModelTuningSample.createEmbeddingModelTuningPipelineJob(
            API_ENDPOINT,
            PROJECT,
            BASE_MODEL_VERSION_ID,
            TASK_TYPE,
            "embedding-customization-pipeline-sample",
            OUTPUT_DIR,
            QUERIES,
            CORPUS,
            TRAIN_LABEL,
            TEST_LABEL,
            BATCH_SIZE,
            ITERATIONS);
    assertThat(pipelineJob.getState()).isNotEqualTo(PipelineState.PIPELINE_STATE_FAILED);
    pipelineJobNames.add(pipelineJob.getName());
  }
}

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

package tpu;

//[START tpu_queued_resources_delete]
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.tpu.v2alpha1.DeleteQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.cloud.tpu.v2alpha1.TpuSettings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.threeten.bp.Duration;
// Uncomment this imports for getting node name
//import com.google.cloud.tpu.v2alpha1.GetQueuedResourceRequest;
//import com.google.cloud.tpu.v2alpha1.QueuedResource;

public class DeleteQueuedResource {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project.
    String projectId = "YOUR_PROJECT_ID";
    // The zone in which the TPU was created.
    String zone = "us-central1-f";
    // The name for your Queued Resource.
    String queuedResourceId = "QUEUED_RESOURCE_ID";

    deleteQueuedResource(projectId, zone, queuedResourceId);
  }

  // Deletes a Queued Resource asynchronously.
  public static void deleteQueuedResource(String projectId, String zone, String queuedResourceId)
      throws ExecutionException, InterruptedException, IOException {
    String name = String.format("projects/%s/locations/%s/queuedResources/%s",
        projectId, zone, queuedResourceId);
    // With these settings the client library handles the Operation's polling mechanism
    // and prevent CancellationException error
    TpuSettings.Builder clientSettings =
        TpuSettings.newBuilder();
    clientSettings
        .deleteQueuedResourceSettings()
        .setRetrySettings(
            RetrySettings.newBuilder()
                .setInitialRetryDelay(Duration.ofMillis(5000L))
                .setRetryDelayMultiplier(2.0)
                .setInitialRpcTimeout(Duration.ZERO)
                .setRpcTimeoutMultiplier(1.0)
                .setMaxRetryDelay(Duration.ofMillis(45000L))
                .setTotalTimeout(Duration.ofHours(24L))
                .build());
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create(clientSettings.build())) {
      // Before deleting the queued resource it is required to delete the TPU VM.
      // For more information about deleting TPU
      // see https://cloud.google.com/tpu/docs/managing-tpus-tpu-vm

      DeleteQueuedResourceRequest request =
              DeleteQueuedResourceRequest.newBuilder().setName(name).build();

      // Waiting for updates in the library. Until then, the operation will complete successfully,
      // but the user will receive an error message with UnknownException and IllegalStateException.
      tpuClient.deleteQueuedResourceAsync(request).get();

      System.out.printf("Deleted Queued Resource: %s\n", name);
    }
  }
}
//[END tpu_queued_resources_delete]

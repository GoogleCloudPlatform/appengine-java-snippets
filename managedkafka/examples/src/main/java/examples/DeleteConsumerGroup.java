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

package examples;

// [START managedkafka_delete_consumergroup]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ConsumerGroupName;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import java.io.IOException;

public class DeleteConsumerGroup {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "us-central1";
    String clusterId = "my-cluster";
    String consumerGroupId = "my-consumer-group";
    deleteConsumerGroup(projectId, region, clusterId, consumerGroupId);
  }

  public static void deleteConsumerGroup(
      String projectId, String region, String clusterId, String consumerGroupId) throws Exception {
    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create()) {
      managedKafkaClient.deleteConsumerGroup(
          ConsumerGroupName.of(projectId, region, clusterId, consumerGroupId));
      System.out.println("Deleted consumer group");
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaClient.getConsumerGroup got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_delete_consumergroup]

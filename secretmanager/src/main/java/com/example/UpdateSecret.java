/*
 * Copyright 2020 Google LLC
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

package com.example;

// [START secretmanager_update_secret]
import com.google.cloud.secretmanager.v1beta1.Secret;
import com.google.cloud.secretmanager.v1beta1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1beta1.SecretName;
import com.google.cloud.secretmanager.v1beta1.UpdateSecretRequest;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;

public class UpdateSecret {

  // Update an existing secret.
  public Secret updateSecret(String projectId, String secretId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the name.
      SecretName name = SecretName.of(projectId, secretId);

      // Create the request.
      UpdateSecretRequest request =
          UpdateSecretRequest.newBuilder()
              .setSecret(
                  Secret.newBuilder()
                      .setName(name.toString())
                      .putLabels("secretmanager", "rocks")
                      .build())
              .setUpdateMask(FieldMaskUtil.fromString("labels"))
              .build();

      // Create the secret.
      Secret updatedSecret = client.updateSecret(request);
      System.out.printf("Updated secret %s\n", updatedSecret.getName());

      return updatedSecret;
    }
  }
}
// [END secretmanager_update_secret]

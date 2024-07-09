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

package secretmanager;

// [START secretmanager_update_regional_secret_with_alias]
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;

public class UpdateRegionalSecretWithAlias {

  public static void updateRegionalSecret() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String secretId = "your-secret-id";
    updateRegionalSecret(projectId, locationId, secretId);
  }

  // Update an existing secret.
  public static void updateRegionalSecret(
      String projectId, String locationId, String secretId) 
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the name.
      SecretName secretName = 
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Build the updated secret.
      Secret.Builder secret =
          Secret.newBuilder()
              .setName(secretName.toString());
      secret.getMutableVersionAliases().put("test", 1L);      

      // Build the field mask.
      FieldMask fieldMask = FieldMaskUtil.fromString("version_aliases");

      // Update the secret.
      Secret updatedSecret = client.updateSecret(secret.build(), fieldMask);
      System.out.printf("Updated alias map: %s\n", 
          updatedSecret.getVersionAliasesMap().toString());
    }
  }
}
// [END secretmanager_update_regional_secret_with_alias]

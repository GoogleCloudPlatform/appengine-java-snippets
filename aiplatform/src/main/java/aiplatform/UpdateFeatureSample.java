/*
 * Copyright 2022 Google LLC
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
 *
 *
 * Update feature. See
 * https://cloud.google.com/vertex-ai/docs/featurestore/setup before running
 * the code snippet
 */

package aiplatform;

// [START aiplatform_update_feature_sample]

import com.google.cloud.aiplatform.v1.Feature;
import com.google.cloud.aiplatform.v1.FeatureName;
import com.google.cloud.aiplatform.v1.FeaturestoreServiceClient;
import com.google.cloud.aiplatform.v1.FeaturestoreServiceSettings;
import com.google.cloud.aiplatform.v1.UpdateFeatureRequest;
import java.io.IOException;

public class UpdateFeatureSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String featurestoreId = "YOUR_FEATURESTORE_ID";
    String entityTypeId = "YOUR_ENTITY_TYPE_ID";
    String featureId = "YOUR_FEATURE_ID";
    String location = "us-central1";
    String endpoint = "us-central1-aiplatform.googleapis.com:443";
    updateFeatureSample(project, featurestoreId, entityTypeId, featureId, location, endpoint);
  }

  static void updateFeatureSample(
      String project,
      String featurestoreId,
      String entityTypeId,
      String featureId,
      String location,
      String endpoint)
      throws IOException {
    FeaturestoreServiceSettings featurestoreServiceSettings =
        FeaturestoreServiceSettings.newBuilder().setEndpoint(endpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (FeaturestoreServiceClient featurestoreServiceClient =
        FeaturestoreServiceClient.create(featurestoreServiceSettings)) {

      Feature feature =
          Feature.newBuilder()
              .setName(
                  FeatureName.of(project, location, featurestoreId, entityTypeId, featureId)
                      .toString())
              .setDescription("sample feature title  updated")
              .build();

      UpdateFeatureRequest request = UpdateFeatureRequest.newBuilder().setFeature(feature).build();
      Feature featureResponse = featurestoreServiceClient.updateFeature(request);
      System.out.println("Update Feature Response");
      System.out.format("Name: %s%n", featureResponse.getName());
      featurestoreServiceClient.close();
    }
  }
}
// [END aiplatform_update_feature_sample]

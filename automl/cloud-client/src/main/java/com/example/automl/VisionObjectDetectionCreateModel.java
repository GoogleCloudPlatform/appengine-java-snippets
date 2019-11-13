/*
 * Copyright 2019 Google LLC
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

package com.example.automl;

// [START automl_vision_object_detection_create_model]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1.AutoMlClient;
import com.google.cloud.automl.v1.ImageObjectDetectionModelMetadata;
import com.google.cloud.automl.v1.LocationName;
import com.google.cloud.automl.v1.Model;
import com.google.cloud.automl.v1.OperationMetadata;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

class VisionObjectDetectionCreateModel {

  static void createModel() throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "YOUR_PROJECT_ID";
    String datasetId = "YOUR_DATASET_ID";
    String displayName = "YOUR_DATASET_NAME";
    createModel(projectId, datasetId, displayName);
  }

  // Create a model
  static void createModel(String projectId, String datasetId, String displayName)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AutoMlClient client = AutoMlClient.create()) {
      // A resource that represents Google Cloud Platform location.
      LocationName projectLocation = LocationName.of(projectId, "us-central1");
      // Set model metadata.
      ImageObjectDetectionModelMetadata metadata =
          ImageObjectDetectionModelMetadata.newBuilder().build();
      Model model =
          Model.newBuilder()
              .setDisplayName(displayName)
              .setDatasetId(datasetId)
              .setImageObjectDetectionModelMetadata(metadata)
              .build();

      // Create a model with the model metadata in the region.
      OperationFuture<Model, OperationMetadata> future =
          client.createModelAsync(projectLocation, model);
      // Don't wait for model creation to finish, as this can take several hours.
      // However, you can use the `name` of the operation to check the status of your model.
      System.out.format("Training operation name: %s\n", future.getInitialFuture().get().getName());
      System.out.println("Training started...");
    }
  }
}
// [END automl_vision_object_detection_create_model]

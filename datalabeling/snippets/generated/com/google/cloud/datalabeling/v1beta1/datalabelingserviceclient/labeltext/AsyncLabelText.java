/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.datalabeling.v1beta1.samples;

// [START datalabeling_v1beta1_generated_datalabelingserviceclient_labeltext_async]
import com.google.api.core.ApiFuture;
import com.google.cloud.datalabeling.v1beta1.DataLabelingServiceClient;
import com.google.cloud.datalabeling.v1beta1.DatasetName;
import com.google.cloud.datalabeling.v1beta1.HumanAnnotationConfig;
import com.google.cloud.datalabeling.v1beta1.LabelTextRequest;
import com.google.longrunning.Operation;

public class AsyncLabelText {

  public static void main(String[] args) throws Exception {
    asyncLabelText();
  }

  public static void asyncLabelText() throws Exception {
    // This snippet has been automatically generated for illustrative purposes only.
    // It may require modifications to work in your environment.
    try (DataLabelingServiceClient dataLabelingServiceClient = DataLabelingServiceClient.create()) {
      LabelTextRequest request =
          LabelTextRequest.newBuilder()
              .setParent(DatasetName.of("[PROJECT]", "[DATASET]").toString())
              .setBasicConfig(HumanAnnotationConfig.newBuilder().build())
              .build();
      ApiFuture<Operation> future =
          dataLabelingServiceClient.labelTextCallable().futureCall(request);
      // Do something.
      Operation response = future.get();
    }
  }
}
// [END datalabeling_v1beta1_generated_datalabelingserviceclient_labeltext_async]

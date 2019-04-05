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

package com.google.healthcare.datasets;

// [START healthcare_deidentify_dataset]
import com.google.HealthcareQuickstart;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare;
import com.google.api.services.healthcare.v1beta1.CloudHealthcareScopes;
import com.google.api.services.healthcare.v1beta1.model.Dataset;
import com.google.api.services.healthcare.v1beta1.model.DeidentifyConfig;
import com.google.api.services.healthcare.v1beta1.model.DeidentifyDatasetRequest;
import com.google.api.services.healthcare.v1beta1.model.DicomConfig;
import com.google.api.services.healthcare.v1beta1.model.TagFilterList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DatasetDeIdentify {
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final Gson GSON = new Gson();
  private static List<String> defaultDicomKeepList = ImmutableList.of("PatientID");

  public static void deidentifyDataset(String sourceDataset, String destinationDataset) throws IOException {
    // String sourceDataset = "your-source-dataset";
    // String destinationDataset = "your-destination-dataset";
    DeidentifyDatasetRequest request = new DeidentifyDatasetRequest();
    request.setDestinationDataset(destinationDatasetName);
    TagFilterList tagFilterList = new TagFilterList();
    List<String> whitelistTagList = Lists.newArrayList(defaultDicomKeepList);
    whitelistTagList.addAll(Lists.newArrayList(whitelistTags));
    tagFilterList.setTags(whitelistTagList);
    DeidentifyConfig deidConfig = new DeidentifyConfig();
    DicomConfig dicomConfig = new DicomConfig();
    dicomConfig.setKeepList(tagFilterList);
    deidConfig.setDicom(dicomConfig);
    request.setConfig(deidConfig);
    HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .deidentify(sourceDatasetName, request)
        .execute();

    Dataset deidDataset =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .get(destinationDatasetName)
            .execute();

    System.out.println("Deidentified Dataset: " + GSON.toJson(deidDataset));
  }

  private static CloudHealthcare createClient() throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information see https://cloud.google.com/docs/authentication/production
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault(HTTP_TRANSPORT, JSON_FACTORY)
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    // Create a HttpRequestInitializer, which will provide a baseline configuration to all requests.
    HttpRequestInitializer requestInitializer =
        request -> {
          credential.initialize(request);
          request.setHeaders(new HttpHeaders().set("X-GFE-SSL", "yes"));
          request.setConnectTimeout(60000); // 1 minute connect timeout
          request.setReadTimeout(60000); // 1 minute read timeout
        };

    // Build the client for interacting with the service.
    return new CloudHealthcare.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
        .setApplicationName("your-application-name")
        .build();
  }
}
// [END healthcare_deidentify_dataset]

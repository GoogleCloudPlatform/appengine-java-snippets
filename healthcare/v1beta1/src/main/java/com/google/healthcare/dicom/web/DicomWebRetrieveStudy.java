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

package com.google.healthcare.dicom.web;

// [START healthcare_dicomweb_retrieve_study]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.HttpBody;
import com.google.gson.Gson;
import java.io.IOException;

public class DicomWebRetrieveStudy {
  private static final Gson GSON = new Gson();

  public static void retrieveStudy(String dicomStoreName, String studyId) throws IOException {
    HttpBody response =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .dicomStores()
            .studies()
            .retrieveStudy(dicomStoreName, "studies/" + studyId)
            .execute();
    System.out.println("Retrieved Dicom study: " + GSON.toJson(response));
  }
}
// [END healthcare_dicomweb_retrieve_study]

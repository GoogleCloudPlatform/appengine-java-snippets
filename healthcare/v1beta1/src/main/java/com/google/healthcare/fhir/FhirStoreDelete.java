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

package com.google.healthcare.fhir;

// [START healthcare_delete_fhir_store]

import com.google.HealthcareQuickstart;

import java.io.IOException;

public class FhirStoreDelete {
  public static void deleteFhirStore(String fhirStoreName) throws IOException {
    HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .fhirStores()
        .delete(fhirStoreName)
        .execute();
  }
}
// [END healthcare_delete_fhir_store]

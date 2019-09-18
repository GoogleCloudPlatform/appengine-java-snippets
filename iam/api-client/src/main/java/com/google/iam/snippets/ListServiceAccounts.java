/* Copyright 2019 Google LLC
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

package com.google.iam.snippets;

// [START iam_list_service_accounts]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.ListServiceAccountsResponse;
import com.google.api.services.iam.v1.model.ServiceAccount;
import java.util.Collections;
import java.util.List;

public class ListServiceAccounts {

  private static Iam service = null;

  // Lists all service accounts for the current project.
  public static List<ServiceAccount> listServiceAccounts(String projectId) throws Exception {
    // String projectId = "my-project-id"

    try {
      initService();
    } catch (Exception e) {
      System.out.println("Failed to build service: \n" + e.toString());
    }
    ListServiceAccountsResponse response =
        service.projects().serviceAccounts().list("projects/" + projectId).execute();
    List<ServiceAccount> serviceAccounts = response.getAccounts();

    for (ServiceAccount account : serviceAccounts) {
      System.out.println("Name: " + account.getName());
      System.out.println("Display Name: " + account.getDisplayName());
      System.out.println("Email: " + account.getEmail());
      System.out.println();
    }
    return serviceAccounts;
  }

  private static void initService() {
    try {
      // Use the Application Default Credentials strategy for authentication. For more info, please
      // see:
      // https://cloud.google.com/docs/authentication/production#finding_credentials_automatically
      GoogleCredential credential =
          GoogleCredential.getApplicationDefault()
              .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));
      // Initialize the IAM service, which can be used to send requests to the IAM API.
      service =
          new Iam.Builder(
                  GoogleNetHttpTransport.newTrustedTransport(),
                  JacksonFactory.getDefaultInstance(),
                  credential)
              .setApplicationName("service-accounts")
              .build();
    } catch (Exception e) {
      System.out.println("Unable to initalize IAM service: \n" + e.toString());
    }
  }
}
// [END iam_list_service_accounts]
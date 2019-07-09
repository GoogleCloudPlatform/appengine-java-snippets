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

package com.google.cloud.gameservices.samples.realms;

// [START cloud_game_servers_realm_list]

import com.google.cloud.gaming.v1alpha.Realm;
import com.google.cloud.gaming.v1alpha.RealmsServiceClient;
import com.google.cloud.gaming.v1alpha.RealmsServiceClient.ListRealmsPagedResponse;

import java.io.IOException;

public class ListRealms {
  public static void listRealms(String projectId, String regionId)
      throws IOException {
    // String projectId = "your-project-id";
    // String regionId = "us-central1-f";
    try (RealmsServiceClient client = RealmsServiceClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, regionId);

      ListRealmsPagedResponse response = client.listRealms(parent);

      for (Realm realm : response.iterateAll()) {
        System.out.println("Realm found: " + realm.getName());
      }
    }
  }
}
// [END cloud_game_servers_realm_list]

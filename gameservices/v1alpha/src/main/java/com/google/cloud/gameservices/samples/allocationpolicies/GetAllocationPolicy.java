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

package com.google.cloud.gameservices.samples.allocationpolicies;

// [START cloud_game_servers_allocation_policy_get]

import com.google.cloud.gaming.v1alpha.AllocationPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.AllocationPolicy;

import java.io.IOException;

public class GetAllocationPolicy {
  public static void getAllocationPolicy(String projectId, String policyId) throws IOException {
    // String projectId = "your-project-id";
    // String policyId = "your-policy-id";
    try (AllocationPoliciesServiceClient client = AllocationPoliciesServiceClient.create()) {
      String policyName = String.format(
          "projects/%s/locations/global/allocationPolicies/%s", projectId, policyId);

      AllocationPolicy allocationPolicy = client.getAllocationPolicy(policyName);

      System.out.println("Allocation Policy found: " + allocationPolicy.getName());
    }
  }
}
// [END cloud_game_servers_allocation_policy_get]

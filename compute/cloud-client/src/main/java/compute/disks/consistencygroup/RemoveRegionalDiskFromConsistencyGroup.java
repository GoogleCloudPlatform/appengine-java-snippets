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

package compute.disks.consistencygroup;

// [START compute_consistency_group_remove_regional_disk]
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.RegionDisksRemoveResourcePoliciesRequest;
import com.google.cloud.compute.v1.RemoveResourcePoliciesRegionDiskRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RemoveRegionalDiskFromConsistencyGroup {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project that contains the disk.
    String project = "YOUR_PROJECT_ID";
    // Region in which your disk and consistency group are located.
    String region = "us-central1";
    // Name of the disk.
    String diskName = "DISK_NAME";
    // Name of the consistency group.
    String consistencyGroupName = "CONSISTENCY_GROUP";

    removeRegionalDiskFromConsistencyGroup(project, region, diskName, consistencyGroupName);
  }

  // Removes a disk from a consistency group.
  public static Status removeRegionalDiskFromConsistencyGroup(
          String project, String region, String diskName, String consistencyGroupName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String consistencyGroupUrl = String.format(
            "https://www.googleapis.com/compute/v1/projects/%s/regions/%s/resourcePolicies/%s",
            project, region, consistencyGroupName);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      RemoveResourcePoliciesRegionDiskRequest request =
              RemoveResourcePoliciesRegionDiskRequest.newBuilder()
                      .setDisk(diskName)
                      .setRegion(region)
                      .setProject(project)
                      .setRegionDisksRemoveResourcePoliciesRequestResource(
                              RegionDisksRemoveResourcePoliciesRequest.newBuilder()
                                      .addAllResourcePolicies(Arrays.asList(consistencyGroupUrl))
                                      .build())
                      .build();

      Operation response = disksClient.removeResourcePoliciesAsync(request)
              .get(1, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Error removing disk from consistency group! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_consistency_group_remove_regional_disk]

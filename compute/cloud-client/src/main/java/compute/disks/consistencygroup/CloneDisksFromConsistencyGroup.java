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

// [START compute_consistency_group_clone]
// If your disk has zonal location uncomment these lines
//import com.google.cloud.compute.v1.DisksClient;
//import com.google.cloud.compute.v1.BulkInsertDiskRequest;
import com.google.cloud.compute.v1.BulkInsertDiskResource;
import com.google.cloud.compute.v1.BulkInsertRegionDiskRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CloneDisksFromConsistencyGroup {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";
    // Name of the region or zone in which your disk is located.
    String disksLocation = "us-central1";
    // Name of the consistency group you want to clone disks from.
    String consistencyGroupName = "YOUR_CONSISTENCY_GROUP_NAME";
    // Name of the region in which your consistency group is located.
    String consistencyGroupLocation = "us-central1";

    cloneDisksFromConsistencyGroup(
            project, disksLocation, consistencyGroupName, consistencyGroupLocation);
  }

  // Clones disks from a consistency group.
  public static void cloneDisksFromConsistencyGroup(String project, String disksLocation,
      String consistencyGroupName, String consistencyGroupLocation)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String sourceConsistencyGroupPolicy = String.format(
            "projects/%s/regions/%s/resourcePolicies/%s", project, consistencyGroupLocation,
            consistencyGroupName);

    // If your disk has zonal location uncomment this code
    //try (DisksClient disksClient = DisksClient.create()){
    //  BulkInsertDiskRequest request = BulkInsertDiskRequest.newBuilder()
    //    .setProject(project)
    //    .setZone(disksLocation)
    //    .setBulkInsertDiskResourceResource(
    //        BulkInsertDiskResource.newBuilder()
    //            .setSourceConsistencyGroupPolicy(sourceConsistencyGroupPolicy)
    //            .build())
    //    .build();
    //Operation response = disksClient.bulkInsertAsync(request).get(3, TimeUnit.MINUTES);

    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {
      BulkInsertRegionDiskRequest request = BulkInsertRegionDiskRequest.newBuilder()
          .setProject(project)
          .setRegion(disksLocation)
          .setBulkInsertDiskResourceResource(
              BulkInsertDiskResource.newBuilder()
                  .setSourceConsistencyGroupPolicy(sourceConsistencyGroupPolicy)
                  .build())
          .build();

      Operation response = regionDisksClient.bulkInsertAsync(request).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.printf("Error cloning disks: %s%n", response.getError());
        return;
      }
      System.out.printf("Disks cloned from consistency group: %s\n", consistencyGroupName);
    }
  }
}
// [END compute_consistency_group_clone]
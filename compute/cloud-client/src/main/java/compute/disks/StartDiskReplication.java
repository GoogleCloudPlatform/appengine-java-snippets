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

package compute.disks;

// [START compute_disk_start_replication]
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.DisksStartAsyncReplicationRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.RegionDisksStartAsyncReplicationRequest;
import com.google.cloud.compute.v1.StartAsyncReplicationDiskRequest;
import com.google.cloud.compute.v1.StartAsyncReplicationRegionDiskRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StartDiskReplication {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // The project that contains the primary disk.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the primary disk.
    String primaryDiskName = "PRIMARY_DISK_NAME";
    // Name of the secondary disk.
    String secondaryDiskName = "SECONDARY_DISK_NAME";
    // Name of the region or zone in which your primary disk is located.
    // Learn more about zones and regions:
    // https://cloud.google.com/compute/docs/disks/async-pd/about#supported_region_pairs
    String primaryDiskLocation = "us-central1-a";
    // Name of the region or zone in which your secondary disk is located.
    String secondaryDiskLocation = "us-east1-b";

    startDiskAsyncReplication(projectId, primaryDiskName, primaryDiskLocation,
            secondaryDiskName, secondaryDiskLocation);
  }

  // Starts asynchronous replication for the specified disk.
  public static Operation.Status startDiskAsyncReplication(String projectId, String primaryDiskName,
         String primaryDiskLocation, String secondaryDiskName, String secondaryDiskLocation)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation response;
    if (Character.isDigit(primaryDiskLocation.charAt(primaryDiskLocation.length() - 1))) {
      String secondaryDiskPath = String.format("projects/%s/regions/%s/disks/%s",
              projectId, secondaryDiskLocation, secondaryDiskName);
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (RegionDisksClient disksClient = RegionDisksClient.create()) {
        RegionDisksStartAsyncReplicationRequest diskRequest =
                RegionDisksStartAsyncReplicationRequest.newBuilder()
                        .setAsyncSecondaryDisk(secondaryDiskPath)
                        .build();
        StartAsyncReplicationRegionDiskRequest request =
                StartAsyncReplicationRegionDiskRequest.newBuilder()
                        .setDisk(primaryDiskName)
                        .setRegionDisksStartAsyncReplicationRequestResource(diskRequest)
                        .setProject(projectId)
                        .setRegion(primaryDiskLocation)
                        .build();
        response = disksClient.startAsyncReplicationAsync(request).get(1, TimeUnit.MINUTES);
      }
    } else {
      String secondaryDiskPath = String.format("projects/%s/zones/%s/disks/%s",
              projectId, secondaryDiskLocation, secondaryDiskName);
      try (DisksClient disksClient = DisksClient.create()) {
        DisksStartAsyncReplicationRequest diskRequest =
                DisksStartAsyncReplicationRequest.newBuilder()
                        .setAsyncSecondaryDisk(secondaryDiskPath)
                        .build();

        StartAsyncReplicationDiskRequest request =
                StartAsyncReplicationDiskRequest.newBuilder()
                        .setDisk(primaryDiskName)
                        .setDisksStartAsyncReplicationRequestResource(diskRequest)
                        .setProject(projectId)
                        .setZone(primaryDiskLocation)
                        .build();
        response = disksClient.startAsyncReplicationAsync(request).get(1, TimeUnit.MINUTES);
      }
    }
    if (response.hasError()) {
      throw new Error("Error starting replication! " + response.getError());
    }
    return response.getStatus();
  }
}
// [END compute_disk_start_replication]
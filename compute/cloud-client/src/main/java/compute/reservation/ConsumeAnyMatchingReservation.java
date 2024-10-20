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

package compute.reservation;

// [START compute_consume_any_matching_reservation]

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.ANY_RESERVATION;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationReservedInstanceProperties;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationAffinity;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConsumeAnyMatchingReservation {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone where the reservation is located.
    String zone = "us-central1-a";
    // Name of the reservation you want to query.
    String reservationName = "YOUR_RESERVATION_NAME";
    // Name of the VM instance you want to query.
    String instanceName = "YOUR_INSTANCE_NAME";
    // Number of the instances.
    int numberOfVms = 2;

    createReservation(projectId, reservationName, numberOfVms, zone);
    createInstance(projectId, zone, instanceName);
  }

  // Creates reservation with properties that match the VM properties.
  public static void createReservation(String projectId, String reservationName,
           int numberOfVms, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Minimum CPU platform of the instances.
    String minCpuPlatform = "Intel Cascade Lake";
    // Machine type of the instances.
    String machineType = "n2-standard-32";
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      Reservation reservation =
          Reservation.newBuilder()
              .setName(reservationName)
              .setZone(zone)
              .setSpecificReservation(
                  AllocationSpecificSKUReservation.newBuilder()
                      .setCount(numberOfVms)
                      .setInstanceProperties(
                          AllocationSpecificSKUAllocationReservedInstanceProperties.newBuilder()
                              .setMachineType(machineType)
                              .setMinCpuPlatform(minCpuPlatform)
                              .build())
                      .build())
              .build();

      // Wait for the create reservation operation to complete.
      Operation response =
          reservationsClient.insertAsync(projectId, zone, reservation)
              .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Reservation creation failed!" + response);
        return;
      }
      System.out.println("Reservation created. Operation Status: " + response.getStatus());
    }
  }

  // Create a new instance with the provided "instanceName" value in the specified project and zone.
  // In this consumption model, existing and new VMs automatically consume a reservation
  // if their properties match the VM properties specified in the reservation.
  public static void createInstance(String projectId, String zone, String instanceName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Below are sample values that can be replaced.
    // machineType: machine type of the VM being created.
    // *   This value uses the format zones/{zone}/machineTypes/{type_name}.
    // *   For a list of machine types, see https://cloud.google.com/compute/docs/machine-types
    // sourceImage: path to the operating system image to mount.
    // *   For details about images you can mount, see https://cloud.google.com/compute/docs/images
    // diskSizeGb: storage size of the boot disk to attach to the instance.
    // networkName: network interface to associate with the instance.
    // Minimum CPU platform of the instances.
    String machineType = String.format("zones/%s/machineTypes/n2-standard-32", zone);
    String sourceImage = String
        .format("projects/debian-cloud/global/images/family/%s", "debian-11");
    long diskSizeGb = 10L;
    String networkName = "default";
    String minCpuPlatform = "Intel Cascade Lake";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Instance creation requires at least one persistent disk and one network interface.
      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(AttachedDisk.Type.PERSISTENT.toString())
              .setDeviceName("disk-1")
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder()
                      .setSourceImage(sourceImage)
                      .setDiskSizeGb(diskSizeGb)
                      .build())
              .build();

      // Use the network interface provided in the networkName argument.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName(networkName)
          .build();

      // Set Reservation Affinity to "ANY"
      ReservationAffinity reservationAffinity =
          ReservationAffinity.newBuilder()
              .setConsumeReservationType(ANY_RESERVATION.toString())
              .build();

      // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an instance.
      Instance instanceResource =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(machineType)
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .setMinCpuPlatform(minCpuPlatform)
              .setReservationAffinity(reservationAffinity)
              .build();

      System.out.printf("Creating instance: %s at %s %n", instanceName, zone);

      // Insert the instance in the specified project and zone.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstanceResource(instanceResource)
          .build();

      OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(
          insertInstanceRequest);

      // Wait for the operation to complete.
      Operation response = operation.get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_consume_any_matching_reservation]
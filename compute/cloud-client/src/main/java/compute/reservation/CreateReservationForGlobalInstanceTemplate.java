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

// [START compute_reservation_create_for_global_instance_template]

import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateReservationForGlobalInstanceTemplate {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the reservation.
    String zone = "us-central1-a";
    // Name of the reservation you want to create.
    String reservationName = "YOUR_RESERVATION_NAME";
    // Name of your instance template.
    String instanceTemplateName = "INSTANCE_TEMPLATE_NAME";
    // The number of virtual machines you want to create.
    int numberOfVms = 3;
    // Optional flag --require-specific-reservation
    // Whether the reservation requires specific reservation.
    boolean specificReservationRequired = true;

    createReservationForGlobalInstanceTemplate(
        projectId, reservationName, instanceTemplateName, numberOfVms, zone, specificReservationRequired);
  }

  // Creates a reservation in a project for the global instance template.
  public static void createReservationForGlobalInstanceTemplate(
      String projectId,
      String reservationName,
      String instanceTemplateName,
      int numberOfVms,
      String zone,
      boolean specificReservationRequired)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      // The URI of the instance template to be used for creating the reservation.
      String instanceTemplateUri =
          String.format("projects/%s/global/instanceTemplates/%s",
              projectId, instanceTemplateName);

      // Create the reservation.
      Reservation reservation =
          Reservation.newBuilder()
              .setName(reservationName)
              .setZone(zone)
              .setSpecificReservationRequired(specificReservationRequired)
              .setSpecificReservation(
                  AllocationSpecificSKUReservation.newBuilder()
                      // Set the number of instances
                      .setCount(numberOfVms)
                      // Set the instance template to be used for creating the reservation.
                      .setSourceInstanceTemplate(instanceTemplateUri)
                      .build())
              .build();

      // Wait for the create reservation operation to complete.
      Operation response =
          reservationsClient.insertAsync(projectId, zone, reservation).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Reservation creation failed!" + response);
        return;
      }
      System.out.println("Reservation created. Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_reservation_create_for_global_instance_template]

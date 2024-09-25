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

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.SPECIFIC_RESERVATION;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.DeleteRegionInstanceTemplateRequest;
import com.google.cloud.compute.v1.InsertRegionInstanceTemplateRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceProperties;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionInstanceTemplatesClient;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import compute.CreateInstanceTemplate;
import compute.DeleteInstance;
import compute.DeleteInstanceTemplate;
import compute.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReservationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static ReservationsClient reservationsClient;
  private static InstancesClient instancesClient;
  private static String RESERVATION_NAME;
  private static String RESERVATION_NAME_GLOBAL;
  private static String RESERVATION_NAME_REGIONAL;
  private static String SHARED_RESERVATION_NAME;
  private static String GLOBAL_INSTANCE_TEMPLATE_URI;
  private static String REGIONAL_INSTANCE_TEMPLATE_URI;
  private static final String GLOBAL_INSTANCE_TEMPLATE_NAME =
      "test-global-inst-temp-" + UUID.randomUUID();
  private static final String REGIONAL_INSTANCE_TEMPLATE_NAME =
      "test-regional-inst-temp-" + UUID.randomUUID();
  private static final String SPECIFIC_SHARED_INSTANCE_NAME =
      "test-shared-instance-" + UUID.randomUUID();
  private static final int NUMBER_OF_VMS = 3;
  private static final String MACHINE_TYPE = "n2-standard-32";
  private static final String MIN_CPU_PLATFORM = "Intel Cascade Lake";

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Initialize the clients once for all tests
    reservationsClient = ReservationsClient.create();
    instancesClient = InstancesClient.create();

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstancesNew(PROJECT_ID, ZONE);
    Util.cleanUpExistingInstanceTemplatesNew(PROJECT_ID);
    Util.cleanUpExistingReservations("test-reserv", PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-reserv-global-", PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-reserv-regional-", PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-shared-reserv-", PROJECT_ID, ZONE);

    RESERVATION_NAME = "test-reserv-" + UUID.randomUUID();
    RESERVATION_NAME_GLOBAL = "test-reserv-global-" + UUID.randomUUID();
    RESERVATION_NAME_REGIONAL = "test-reserv-regional-" + UUID.randomUUID();
    SHARED_RESERVATION_NAME = "test-shared-reserv-" + UUID.randomUUID();

    GLOBAL_INSTANCE_TEMPLATE_URI = String.format("projects/%s/global/instanceTemplates/%s",
        PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME);
    String region = ZONE.substring(0, ZONE.lastIndexOf('-'));
    REGIONAL_INSTANCE_TEMPLATE_URI =
        String.format("projects/%s/regions/%s/instanceTemplates/%s",
            PROJECT_ID, region, REGIONAL_INSTANCE_TEMPLATE_NAME);

    // Create instance template with GLOBAL location.
    CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance Template Operation Status " + GLOBAL_INSTANCE_TEMPLATE_NAME);
    // Create instance template with REGIONAL location.
    ReservationIT.createRegionalInstanceTemplate(
        PROJECT_ID, REGIONAL_INSTANCE_TEMPLATE_NAME, ZONE);
    assertThat(stdOut.toString()).contains("Instance Template Operation Status: DONE");

    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Delete instance template with GLOBAL location.
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for "
            + GLOBAL_INSTANCE_TEMPLATE_NAME);

    // Delete instance template with REGIONAL location.
    ReservationIT.deleteRegionalInstanceTemplate(
        PROJECT_ID, ZONE, REGIONAL_INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for "
            + REGIONAL_INSTANCE_TEMPLATE_NAME);

    // Delete instance template created for consume shared reservation.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE_NAME);

    // Delete all reservations created for testing.
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_GLOBAL);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_REGIONAL);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, SHARED_RESERVATION_NAME);

    // Test that the reservation is deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME, ZONE));

    // Close clients after all tests
    reservationsClient.close();
    instancesClient.close();

    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void firstCreateReservationTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservation.createReservation(
        PROJECT_ID, RESERVATION_NAME, NUMBER_OF_VMS, ZONE);

    Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME);

    Assert.assertEquals(RESERVATION_NAME, reservation.getName());
    Assert.assertEquals(NUMBER_OF_VMS,
        reservation.getSpecificReservation().getCount());
    Assert.assertTrue(reservation.getZone().contains(ZONE));
  }

  @Test
  public void secondGetReservationTest()
      throws IOException {
    Reservation reservation = GetReservation.getReservation(
        PROJECT_ID, RESERVATION_NAME, ZONE);

    assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME);
  }

  @Test
  public void thirdListReservationTest() throws IOException {
    List<Reservation> reservations =
        ListReservations.listReservations(PROJECT_ID, ZONE);

    assertThat(reservations).isNotNull();
    Assert.assertTrue(reservations.get(0).getName().contains("test-reserv"));
    Assert.assertTrue(reservations.get(1).getName().contains("test-reserv"));
    Assert.assertTrue(reservations.get(2).getName().contains("test-reserv"));
  }

  @Test
  public void firstCreateReservationWithGlobalInstanceTemplateTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservationForInstanceTemplate.createReservationForInstanceTemplate(
        PROJECT_ID, RESERVATION_NAME_GLOBAL,
        GLOBAL_INSTANCE_TEMPLATE_URI, NUMBER_OF_VMS, ZONE);

    Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME_GLOBAL);

    Assert.assertTrue(reservation.getSpecificReservation()
        .getSourceInstanceTemplate().contains(GLOBAL_INSTANCE_TEMPLATE_NAME));
    Assert.assertEquals(RESERVATION_NAME_GLOBAL, reservation.getName());
  }

  @Test
  public void firstCreateReservationWithRegionInstanceTemplateTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservationForInstanceTemplate.createReservationForInstanceTemplate(
        PROJECT_ID, RESERVATION_NAME_REGIONAL, REGIONAL_INSTANCE_TEMPLATE_URI,
        NUMBER_OF_VMS, ZONE);
    Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME_REGIONAL);

    Assert.assertTrue(reservation.getSpecificReservation()
        .getSourceInstanceTemplate().contains(REGIONAL_INSTANCE_TEMPLATE_NAME));
    Assert.assertTrue(reservation.getZone().contains(ZONE));
    Assert.assertEquals(RESERVATION_NAME_REGIONAL, reservation.getName());
  }

  @Test
  public void testConsumeSpecificSharedReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ConsumeSpecificSharedReservation.createReservation(PROJECT_ID,
        SHARED_RESERVATION_NAME, NUMBER_OF_VMS, ZONE,
        MACHINE_TYPE, MIN_CPU_PLATFORM, true);

    Assert.assertEquals(SHARED_RESERVATION_NAME,
        reservationsClient.get(PROJECT_ID, ZONE, SHARED_RESERVATION_NAME).getName());

    ConsumeSpecificSharedReservation.createInstance(
        PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE_NAME, MACHINE_TYPE,
        MIN_CPU_PLATFORM, SHARED_RESERVATION_NAME);

    // Verify that the instance was created with the correct reservation and consumeReservationType
    Instance instance = instancesClient.get(PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE_NAME);

    Assert.assertTrue(instance.getReservationAffinity()
        .getValuesList().get(0).contains(SHARED_RESERVATION_NAME));
    Assert.assertEquals(SPECIFIC_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }

  // Creates a new instance template with the REGIONAL location.
  public static void createRegionalInstanceTemplate(
      String projectId, String templateName, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RegionInstanceTemplatesClient templatesClientRegion =
             RegionInstanceTemplatesClient.create()) {

      String machineType = "n1-standard-1"; // Example machine type
      String sourceImage = "projects/debian-cloud/global/images/family/debian-11"; // Example image
      String region = zone.substring(0, zone.lastIndexOf('-')); // Extract the region from the zone

      // Define the boot disk for the instance template
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setSourceImage(sourceImage)
              .setDiskType("pd-balanced") // Example disk type
              .setDiskSizeGb(100L) // Example disk size
              .build())
          .setAutoDelete(true)
          .setBoot(true)
          .build();

      // Define the network interface for the instance template
      // Note: The subnetwork must be in the same region as the instance template.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName("my-network-test")
          .setSubnetwork(String.format("projects/%s/regions/%s/subnetworks/default",
              PROJECT_ID, region))
          .build();

      // Define the instance properties for the template
      InstanceProperties instanceProperties = InstanceProperties.newBuilder()
          .addDisks(attachedDisk)
          .setMachineType(machineType)
          .addNetworkInterfaces(networkInterface)
          .build();

      // Build the instance template object
      InstanceTemplate instanceTemplate = InstanceTemplate.newBuilder()
          .setName(templateName)
          .setProperties(instanceProperties)
          .build();

      // Create the request to insert the instance template
      InsertRegionInstanceTemplateRequest insertInstanceTemplateRequest =
          InsertRegionInstanceTemplateRequest
              .newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setInstanceTemplateResource(instanceTemplate)
              .build();

      // Send the request and wait for the operation to complete
      Operation response = templatesClientRegion.insertAsync(insertInstanceTemplateRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance Template creation failed! " + response);
        return;
      }
      System.out.printf("Instance Template Operation Status: %s%n", response.getStatus());
    }
  }

  // Delete an instance template with the REGIONAL location.
  private static void deleteRegionalInstanceTemplate(
      String projectId, String zone, String templateName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RegionInstanceTemplatesClient regionInstanceTemplatesClient =
             RegionInstanceTemplatesClient.create()) {
      String region = zone.substring(0, zone.lastIndexOf('-')); // Extract the region from the zone

      DeleteRegionInstanceTemplateRequest deleteInstanceTemplateRequest =
          DeleteRegionInstanceTemplateRequest
              .newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setInstanceTemplate(templateName)
              .build();

      Operation response = regionInstanceTemplatesClient.deleteAsync(
          deleteInstanceTemplateRequest).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance template deletion failed ! ! " + response);
        return;
      }
      System.out.printf("Instance template deletion operation status for %s: %s ", templateName,
          response.getStatus());
    }
  }
}
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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static compute.Util.getZone;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import com.google.cloud.compute.v1.ShareSettings;
import com.google.cloud.compute.v1.ShareSettingsProjectConfig;
import compute.CreateInstanceTemplate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class ReservationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = getZone();
  private static String RESERVATION_NAME;
  private static final int NUMBER_OF_VMS = 3;
  private static String INSTANCE_TEMPLATE_URI;

  private ByteArrayOutputStream stdOut;

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

    RESERVATION_NAME = "test-reserv-" + UUID.randomUUID();

    String instanceTemplateName = "test-inst-for-shared-res" + UUID.randomUUID();
    CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, instanceTemplateName);
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {
      Assert.assertTrue(instanceTemplatesClient.get(PROJECT_ID, instanceTemplateName)
          .getName().contains(instanceTemplateName));
    }
    INSTANCE_TEMPLATE_URI = String.format(
        "projects/%s/global/instanceTemplates/%s", PROJECT_ID, instanceTemplateName);
  }

  @BeforeEach
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterEach
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Verify reservation is deleted
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    assertThat(stdOut.toString()).contains("Deleted reservation: " + RESERVATION_NAME);
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      // Get the reservation.
      Assertions.assertThrows(
          NotFoundException.class,
          () -> reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME));
    }

    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void testCreateReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservation.createReservation(
        PROJECT_ID, RESERVATION_NAME, NUMBER_OF_VMS, ZONE);

    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME);

      assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
      Assert.assertEquals(RESERVATION_NAME, reservation.getName());
      Assert.assertEquals(NUMBER_OF_VMS,
          reservation.getSpecificReservation().getCount());
      Assert.assertTrue(reservation.getZone().contains(ZONE));
    }
  }

  @Test
  public void testCreateSharedReservation()
      throws ExecutionException, InterruptedException, TimeoutException {

    // Mock the ReservationsClient
    ReservationsClient mockReservationsClient = mock(ReservationsClient.class);

    ShareSettings shareSettings = ShareSettings.newBuilder()
        .setShareType(String.valueOf(ShareSettings.ShareType.SPECIFIC_PROJECTS))
        .putProjectMap("CONSUMER_PROJECT_ID_1", ShareSettingsProjectConfig.newBuilder().build())
        .putProjectMap("CONSUMER_PROJECT_ID_2", ShareSettingsProjectConfig.newBuilder().build())
        .build();

    Reservation reservation =
        Reservation.newBuilder()
            .setName(RESERVATION_NAME)
            .setZone(ZONE)
            .setSpecificReservationRequired(true)
            .setShareSettings(shareSettings)
            .setSpecificReservation(
                AllocationSpecificSKUReservation.newBuilder()
                    .setCount(NUMBER_OF_VMS)
                    .setSourceInstanceTemplate(INSTANCE_TEMPLATE_URI)
                    .build())
            .build();

    OperationFuture mockFuture = mock(OperationFuture.class);
    when(mockReservationsClient.insertAsync(PROJECT_ID, ZONE, reservation))
        .thenReturn(mockFuture);

    // Mock the Operation
    Operation mockOperation = mock(Operation.class);
    when(mockFuture.get(3, TimeUnit.MINUTES)).thenReturn(mockOperation);
    when(mockOperation.hasError()).thenReturn(false);
    when(mockOperation.getStatus()).thenReturn(Operation.Status.DONE);

    // Create an instance, passing in the mock client
    CreateSharedReservation creator = new CreateSharedReservation(mockReservationsClient);

    creator.createSharedReservation(PROJECT_ID, ZONE,
        RESERVATION_NAME, INSTANCE_TEMPLATE_URI, NUMBER_OF_VMS);

    verify(mockReservationsClient, times(1))
        .insertAsync(PROJECT_ID, ZONE, reservation);
    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
  }
}
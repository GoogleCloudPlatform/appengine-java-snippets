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

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.NO_RESERVATION;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import compute.DeleteInstance;
import compute.Util;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ConsumeReservationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-southwest1-a";
  private static InstancesClient instancesClient;
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static final String INSTANCE_NOT_CONSUME_RESERVATION_NAME =
      "test-instance-not-consume-"  + javaVersion  + "-"
          + UUID.randomUUID().toString().substring(0, 8);
  private static final String MACHINE_TYPE = "n2-standard-32";

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

    // Initialize the clients once for all tests
    instancesClient = InstancesClient.create();

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("test-instance-not-consume-"  + javaVersion, PROJECT_ID, ZONE);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Delete the instance created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NOT_CONSUME_RESERVATION_NAME);

    // Close the client after all tests
    instancesClient.close();
  }

  @Test
  public void testCreateInstanceNotConsumeReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    CreateInstanceNotConsumeReservation.createInstanceNotConsumeReservation(
        PROJECT_ID, ZONE, INSTANCE_NOT_CONSUME_RESERVATION_NAME, MACHINE_TYPE);

    // Verify that the instance was created with the correct consumeReservationType
    Instance instance = instancesClient.get(
        PROJECT_ID, ZONE, INSTANCE_NOT_CONSUME_RESERVATION_NAME);

    Assertions.assertEquals(NO_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }
}

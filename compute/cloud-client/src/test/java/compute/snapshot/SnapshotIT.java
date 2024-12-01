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

package compute.snapshot;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 6, unit = TimeUnit.MINUTES)
public class SnapshotIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "asia-south1-a";
  private static final String REGION = ZONE.substring(0, ZONE.lastIndexOf('-'));
  static String templateUUID = UUID.randomUUID().toString();
  private static final String SCHEDULE_NAME = "test-schedule-" + templateUUID;
  private static final  String SCHEDULE_DESCRIPTION = "Test hourly snapshot schedule";
  private static final int MAX_RETENTION_DAYS = 2;
  private static final String STORAGE_LOCATION = "US";
  private static final  String ON_SOURCE_DISK_DELETE = "KEEP_AUTO_SNAPSHOTS";

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
  }

  @AfterAll
  public static void cleanup()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Delete snapshot schedule created for testing.
    Operation.Status status = DeleteSnapshotSchedule
            .deleteSnapshotSchedule(PROJECT_ID, REGION, SCHEDULE_NAME);

    assertThat(status).isEqualTo(Operation.Status.DONE);
  }

  @Test
  public void testCreateSnapshotScheduleHourly()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation.Status status = CreateSnapshotSchedule.createSnapshotSchedule(
            PROJECT_ID, REGION, SCHEDULE_NAME, SCHEDULE_DESCRIPTION,
            MAX_RETENTION_DAYS, STORAGE_LOCATION, ON_SOURCE_DISK_DELETE);

    assertThat(status).isEqualTo(Operation.Status.DONE);
  }
}

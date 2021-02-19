/*
 * Copyright 2020 Google LLC
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

package com.example.cloud.bigtable.scheduledbackups;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.bigtable.admin.v2.BigtableInstanceAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.models.Cluster;
import com.google.cloud.bigtable.admin.v2.models.CreateClusterRequest;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import com.google.cloud.bigtable.admin.v2.models.StorageType;
import com.google.gson.Gson;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedRunnable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class CreateBackupTestIT {
  private static final String PROJECT_ENV = "GOOGLE_CLOUD_PROJECT";
  private static final String INSTANCE_ENV = "BIGTABLE_TESTING_INSTANCE";
  private static final String CLUSTER_ID = "cl-" + UUID.randomUUID().toString().substring(0, 10);
  private static final String TABLE_ID = "tbl-" + UUID.randomUUID().toString().substring(0, 10);
  private static final String ZONE_ID = "us-east1-b";
  private static final String COLUMN_FAMILY_NAME = "cf1";

  private static String projectId;
  private static String instanceId;

  // Root URL pointing to the locally hosted function
  // The Functions Framework Maven plugin lets us run a function locally
  private static final String BASE_URL = "http://localhost:8080";

  private static Process emulatorProcess = null;
  private static HttpClient client = HttpClientBuilder.create().build();
  private static final Gson gson = new Gson();

  private static String requireEnv(String varName) {
    assertNotNull(
        System.getenv(varName),
        "Environment variable '%s' is required to perform these tests.".format(varName));
    return System.getenv(varName);
  }

  @BeforeClass
  public static void setUp() throws IOException {
    projectId = requireEnv(PROJECT_ENV);
    instanceId = requireEnv(INSTANCE_ENV);

    try (BigtableInstanceAdminClient instanceAdmin =
        BigtableInstanceAdminClient.create(projectId)) {
      // Create a cluster.
      Cluster cluster = instanceAdmin.createCluster(
          CreateClusterRequest.of(instanceId, CLUSTER_ID)
              .setZone(ZONE_ID)
              .setServeNodes(1)
              .setStorageType(StorageType.SSD));
    } catch (IOException e) {
      System.out.println("Error during BeforeClass while creating cluster: \n" + e.toString());
      throw(e);
    }

    try (BigtableTableAdminClient tableAdmin =
        BigtableTableAdminClient.create(projectId, instanceId)) {
      // Create a table.
      tableAdmin.createTable(CreateTableRequest.of(TABLE_ID).addFamily(COLUMN_FAMILY_NAME));
    } catch (IOException e) {
      System.out.println("Error during BeforeClass while creating table: \n" + e.toString());
      throw(e);
    }

    // Get the sample's base directory (the one containing a pom.xml file)
    String baseDir = System.getProperty("basedir");

    // Emulate the function locally by running the Functions Framework Maven plugin
    emulatorProcess = new ProcessBuilder()
        .command("mvn", "function:run")
        .directory(new File(baseDir))
        .start();
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    try (BigtableTableAdminClient tableAdmin =
        BigtableTableAdminClient.create(projectId, instanceId)) {
      for (String backup : tableAdmin.listBackups(CLUSTER_ID)) {
        tableAdmin.deleteBackup(CLUSTER_ID, backup);
      }
      tableAdmin.deleteTable(TABLE_ID);
    } catch (IOException e) {
      System.out.println("Error during AfterClass while deleting backup and table: \n"
                             + e.toString());
      throw(e);
    }

    try (BigtableInstanceAdminClient instanceAdmin =
        BigtableInstanceAdminClient.create(projectId)) {
      instanceAdmin.deleteCluster(instanceId, CLUSTER_ID);
    } catch (IOException e) {
      System.out.println("Error during AfterClass while deleting cluster: \n" + e.toString());
      throw (e);
    }
    // Terminate the running Functions Framework Maven plugin process (if it's still running)
    if (emulatorProcess.isAlive()) {
      emulatorProcess.destroy();
    }
  }

  @Test
  public void testCreateBackup() throws Throwable {
    String functionUrl = BASE_URL + "/createBackup";
    String msg = String.format(
        "{\"projectId\":\"%s\", \"instanceId\":\"%s\", \"tableId\":\"%s\", \"clusterId\":\"%s\","
            + "\"expireHours\":%d}",
        projectId, instanceId, TABLE_ID, CLUSTER_ID, 8);
    String msgBase64 = Base64.getEncoder().encodeToString(msg.getBytes(StandardCharsets.UTF_8));
    Map<String, String> msgMap = new HashMap<>();
    msgMap.put("data", msgBase64);
    Map<String, Map<String, String>> dataMap = new HashMap<>();
    dataMap.put("data", msgMap);
    String jsonStr = gson.toJson(dataMap);

    HttpPost postRequest =  new HttpPost(URI.create(functionUrl));
    postRequest.setEntity(new StringEntity(jsonStr));

    // The Functions Framework Maven plugin process takes time to start up
    // Use resilience4j to retry the test HTTP request until the plugin responds
    RetryRegistry registry = RetryRegistry.of(RetryConfig.custom()
        .maxAttempts(8)
        .retryExceptions(HttpHostConnectException.class)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(200, 2))
        .build());
    Retry retry = registry.retry("my");

    // Perform the request-retry process
    CheckedRunnable retriableFunc = Retry.decorateCheckedRunnable(
        retry, () -> client.execute(postRequest));
    retriableFunc.run();
    // Wait 2 mins for the backup to be created.
    TimeUnit.MINUTES.sleep(2);
    // Check if backup exists
    List<String> backups = new ArrayList<String>();
    try (BigtableTableAdminClient tableAdmin =
        BigtableTableAdminClient.create(projectId, instanceId)) {
      backups = tableAdmin.listBackups(CLUSTER_ID);
    } catch (IOException e) {
      System.out.println("Unable to list backups: \n" + e.toString());
      throw (e);
    }
    assertThat(backups.size()).isEqualTo(1);
    String expectedBackupPrefix = TABLE_ID + "-backup-";
    assertThat(backups.get(0).contains(expectedBackupPrefix));
  }
}

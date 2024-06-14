/*
 * Copyright 2022 Google LLC
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

package compute;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static compute.Util.getZone;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class InstanceTemplatesIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String DEFAULT_ZONE = "us-central1-a";
  private static final String DEFAULT_REGION = DEFAULT_ZONE.substring(0, DEFAULT_ZONE.length() - 2);
  private static String TEMPLATE_NAME;
  private static String TEMPLATE_NAME_WITH_DISK;
  private static String TEMPLATE_NAME_FROM_INSTANCE;
  private static String TEMPLATE_NAME_WITH_SUBNET;
  private static String MACHINE_NAME_CR;
  private static String MACHINE_NAME_CR_TEMPLATE;
  private static String MACHINE_NAME_CR_TEMPLATE_OR;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    String templateUUID = UUID.randomUUID().toString();
    TEMPLATE_NAME = "test-csam-template-" + templateUUID;
    TEMPLATE_NAME_WITH_DISK = "test-csam-template-disk-" + templateUUID;
    TEMPLATE_NAME_FROM_INSTANCE = "test-csam-template-inst-" + templateUUID;
    TEMPLATE_NAME_WITH_SUBNET = "test-csam-template-snet-" + templateUUID;
    String instanceUUID = UUID.randomUUID().toString();
    MACHINE_NAME_CR = "test-csam-instance" + instanceUUID;
    MACHINE_NAME_CR_TEMPLATE = "test-csam-inst-template-" + instanceUUID;
    MACHINE_NAME_CR_TEMPLATE_OR =
        "test-csam-inst-temp-or-" + instanceUUID;

    // Check for resources created >24hours which haven't been deleted in the project.
    Util.cleanUpExistingInstanceTemplates("test-csam-", PROJECT_ID);
    Util.cleanUpExistingInstances("test-csam-", PROJECT_ID, DEFAULT_ZONE);
    Util.cleanUpExistingInstances("bulkInsert-", PROJECT_ID, DEFAULT_ZONE);

    // Create templates.
    CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    assertThat(stdOut.toString()).contains("Instance Template Operation Status " + TEMPLATE_NAME);
    CreateInstance.createInstance(PROJECT_ID, DEFAULT_ZONE, MACHINE_NAME_CR);
    TimeUnit.SECONDS.sleep(30);
    CreateTemplateFromInstance.createTemplateFromInstance(PROJECT_ID, TEMPLATE_NAME_FROM_INSTANCE,
        getInstance(DEFAULT_ZONE, MACHINE_NAME_CR).getSelfLink());
    assertThat(stdOut.toString())
        .contains("Instance Template creation operation status " + TEMPLATE_NAME_FROM_INSTANCE);
    CreateTemplateWithSubnet.createTemplateWithSubnet(PROJECT_ID, "global/networks/default",
        String.format("regions/%s/subnetworks/default", DEFAULT_REGION), TEMPLATE_NAME_WITH_SUBNET);
    assertThat(stdOut.toString())
        .contains("Template creation from subnet operation status " + TEMPLATE_NAME_WITH_SUBNET);
    TimeUnit.SECONDS.sleep(30);

    // Create instances.
    CreateInstanceFromTemplate.createInstanceFromTemplate(PROJECT_ID, DEFAULT_ZONE,
        MACHINE_NAME_CR_TEMPLATE,
        TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance creation from template: Operation Status " + MACHINE_NAME_CR_TEMPLATE);
    CreateInstanceTemplate.createInstanceTemplateWithDiskType(PROJECT_ID, TEMPLATE_NAME_WITH_DISK);
    TimeUnit.SECONDS.sleep(30);

    CreateInstanceFromTemplateWithOverrides
        .createInstanceFromTemplateWithOverrides(PROJECT_ID, DEFAULT_ZONE,
            MACHINE_NAME_CR_TEMPLATE_OR,
            TEMPLATE_NAME_WITH_DISK);
    assertThat(stdOut.toString()).contains(
        "Instance creation from template with overrides: Operation Status "
            + MACHINE_NAME_CR_TEMPLATE_OR);
    Assert.assertEquals(
        getInstance(DEFAULT_ZONE, MACHINE_NAME_CR_TEMPLATE_OR).getDisksCount(), 2);
    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    // Delete instances.
    DeleteInstance.deleteInstance(PROJECT_ID, DEFAULT_ZONE, MACHINE_NAME_CR);
    DeleteInstance.deleteInstance(PROJECT_ID, DEFAULT_ZONE, MACHINE_NAME_CR_TEMPLATE);
    DeleteInstance.deleteInstance(PROJECT_ID, DEFAULT_ZONE, MACHINE_NAME_CR_TEMPLATE_OR);
    // Delete instance templates.
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + TEMPLATE_NAME);
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_WITH_DISK);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + TEMPLATE_NAME_WITH_DISK);
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_FROM_INSTANCE);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + TEMPLATE_NAME_FROM_INSTANCE);
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_WITH_SUBNET);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + TEMPLATE_NAME_WITH_SUBNET);
    stdOut.close();
    System.setOut(out);
  }

  public static Instance getInstance(String zone, String instanceName) throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      return instancesClient.get(PROJECT_ID, zone, instanceName);
    }
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


  @Test
  public void testGetInstanceTemplate() throws IOException {
    GetInstanceTemplate.getInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME);
    GetInstanceTemplate.getInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_FROM_INSTANCE);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME_FROM_INSTANCE);
    GetInstanceTemplate.getInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_WITH_SUBNET);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME_WITH_SUBNET);
  }

  @Test
  public void testListInstanceTemplates() throws IOException {
    ListInstanceTemplates.listInstanceTemplates(PROJECT_ID);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME_FROM_INSTANCE);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME_WITH_SUBNET);
  }

  @Test
  public void testCreateInstanceBulkInsert() {
    List<Instance> instances = new ArrayList<>();
    try {
      String id = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
      String namePattern = "bulkInsert-##-" + id;
      instances = CreateInstanceBulkInsert
              .bulkInsertInstance(PROJECT_ID, DEFAULT_ZONE, TEMPLATE_NAME,
                      2, namePattern, 2, new HashMap<>());
      Assert.assertEquals(2, instances.size());
      Assert.assertTrue(instances.stream().allMatch(instance
              -> instance.getName().contains("bulkInsert-")));
      Assert.assertTrue(instances.stream().allMatch(instance -> instance.getName().contains(id)));
    } catch (Exception e) {
      System.err.println(e.getCause().toString());
      Assert.fail();
    } finally {
      for (Instance instance : instances) {
        try {
          DeleteInstance.deleteInstance(PROJECT_ID, DEFAULT_ZONE, instance.getName());
        } catch (Exception e) {
          System.err.printf("Can't delete instance - %s. Cause by {%s}",
                  instance.getName(), e.getMessage());
        }
      }
    }
  }
}
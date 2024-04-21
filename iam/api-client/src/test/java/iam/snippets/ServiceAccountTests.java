/* Copyright 2018 Google LLC
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

package iam.snippets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.iam.admin.v1.ServiceAccountKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceAccountTests {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String SERVICE_ACCOUNT =
      "service-account-" + UUID.randomUUID().toString().substring(0, 8);
  private static String SERVICE_ACCOUNT_KEY;
  private ByteArrayOutputStream bout;
  private final PrintStream originalOut = System.out;

  @Rule public MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static void requireEnvVar(String varName) {
    assertNotNull(
        System.getenv(varName),
        String.format("Environment variable '%s' is required to perform these tests.", varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void stage1_testServiceAccountCreate() {
    CreateServiceAccount.createServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
    String got = bout.toString();
    assertThat(got, containsString("Created service account: " + SERVICE_ACCOUNT));
  }

  @Test
  public void stage1_testServiceAccountsList() throws IOException {
    IAMClient.ListServiceAccountsPagedResponse response =
            ListServiceAccounts.listServiceAccounts(PROJECT_ID);
    assertTrue(response.iterateAll().iterator().hasNext());
  }

  @Test
  public void stage2_testServiceAccountRename() {
    RenameServiceAccount.renameServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
    String got = bout.toString();
    assertThat(got, containsString("Updated display name"));
  }

  @Test
  public void stage2_testServiceAccountKeyCreate() throws IOException {
    ServiceAccountKey key = CreateServiceAccountKey.createKey(PROJECT_ID, SERVICE_ACCOUNT);
    SERVICE_ACCOUNT_KEY = CreateServiceAccountKey.extractKeyName(key);

    assertNotNull(SERVICE_ACCOUNT_KEY);
  }

  @Test
  public void stage2_testServiceAccountKeysList() throws IOException {
    List<ServiceAccountKey> keys = ListServiceAccountKeys.listKeys(PROJECT_ID, SERVICE_ACCOUNT);

    assertNotEquals(0, keys.size());
    assertTrue(keys.stream()
            .map(CreateServiceAccountKey::extractKeyName)
            .anyMatch(keyName -> keyName.equals(SERVICE_ACCOUNT_KEY)));
  }

  @Test
  public void stage2_testServiceAccountKeyDisable() {
    DisableServiceAccountKey
        .disableServiceAccountKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY);
    String got = bout.toString();
    assertThat(got, containsString("Disabled service account key"));
  }

  @Test
  public void stage2_testServiceAccountKeyEnable() {
    EnableServiceAccountKey
        .enableServiceAccountKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY);
    String got = bout.toString();
    assertThat(got, containsString("Enabled service account key"));
  }

  @Test
  public void stage3_testServiceAccountKeyDelete() {
    DeleteServiceAccountKey.deleteKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY);
    String got = bout.toString();
    assertThat(got, containsString("Deleted key:"));
  }

  @Test
  public void stage4_testDisableServiceAccount() throws IOException {
    DisableServiceAccount.disableServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);

    String got = bout.toString();
    assertThat(got, containsString("Disabled service account:"));
  }

  @Test
  public void stage5_testEnableServiceAccount() throws IOException {
    EnableServiceAccount.enableServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);

    String got = bout.toString();
    assertThat(got, containsString("Enabled service account:"));
  }

  @Test
  public void stage6_testServiceAccountDelete() {
    DeleteServiceAccount.deleteServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
    String got = bout.toString();
    assertThat(got, containsString("Deleted service account:"));
  }
}

/*
 * Copyright 2023 Google LLC
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

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.DeleteRoleRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class RoleIT {
  private ByteArrayOutputStream bout;

  private static final String projectId = System.getenv("IAM_PROJECT_ID");
  private static final String _suffix = UUID.randomUUID().toString().substring(0, 6);
  private static final String roleId = "testRole" + _suffix;
  private static final String roleName = "projects/" + projectId + "/roles/" + roleId;

  private static void requireEnvVar() {
    assertNotNull(
        "Environment variable " + "GOOGLE_CLOUD_PROJECT" + " is required to perform these tests.",
        System.getenv("GOOGLE_CLOUD_PROJECT"));
  }

  @Rule public Timeout globalTimeout = Timeout.seconds(300); // 5 minute timeout

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar();
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @AfterClass
  public static void cleanUp() {
    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.deleteRole(DeleteRoleRequest.newBuilder().setName(roleName).build());
    } catch (NotFoundException e) {
      System.out.println("Role no longer exists already.");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testRole() {
    // Test get role.
    GetRole.getRole("roles/iam.roleViewer");
    assertThat(bout.toString().contains("iam.roles.get"));

    bout.reset();
    // Test create role.
    CreateRole.createRole(
        projectId,
        "Java Sample Custom Role",
        "Pass",
        Arrays.asList("iam.roles.get", "iam.roles.list"),
        roleId);
    assertThat(bout.toString().contains("javaSampleCustomRole"));

    bout.reset();
    // Test edit role.
    EditRole.editRole(projectId, roleId);
    assertThat(bout.toString().contains("stage: DISABLED"));

    bout.reset();
    // Test list roles.
    ListRoles.listRoles(projectId);
    assertThat(bout.toString().contains(roleId));

    bout.reset();
    // Test delete role.
    DeleteRole.deleteRole(projectId, roleId);
    assertThat(bout.toString().contains("Role deleted"));

    bout.reset();
    // Test undelete role.
    UndeleteRole.undeleteRole(projectId, roleId);
    assertThat(bout.toString().contains("Undeleted role"));

    bout.reset();
    // Test query testable permissions.
    QueryTestablePermissions.queryTestablePermissions(projectId);
    assertThat(bout.toString().contains("iam.roles.get"));
  }
}

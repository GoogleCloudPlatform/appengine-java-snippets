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

// [START iam_edit_role]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.Role;
import com.google.iam.admin.v1.Role.RoleLaunchStage;
import com.google.iam.admin.v1.UpdateRoleRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

/** Edit role metadata. Specially, changing launch stage enum to GA. */
public class EditRole {

  public static void main(String[] args) {
    // TODO(developer): Replace the variables before running the sample.
    // Role ID must point to an existing role.
    String projectId = "your-project-id";
    String roleId = "a unique identifier (e.g. testViewer)";

    editRole(projectId, roleId);
  }

  public static void editRole(String projectId, String roleId) {

    String roleName = "projects/" + projectId + "/roles/" + roleId;

    Role.Builder roleBuilder = Role.newBuilder().setName(roleName).setStage(RoleLaunchStage.GA);

    FieldMask fieldMask = FieldMask.newBuilder().addPaths("stage").build();

    UpdateRoleRequest updateRoleRequest =
        UpdateRoleRequest.newBuilder()
            .setName(roleName)
            .setRole(roleBuilder)
            .setUpdateMask(fieldMask)
            .build();

    // Initialize client for sending requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      Role result = iamClient.updateRole(updateRoleRequest);
      System.out.println("Edited role:\n" + result);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
// [END iam_edit_role]

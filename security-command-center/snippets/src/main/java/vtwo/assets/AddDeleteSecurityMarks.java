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

// [START securitycenter_add_delete_security_marks_assets_v2]
package vtwo.assets;

import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityMarks;
import com.google.cloud.securitycenter.v2.UpdateSecurityMarksRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class AddDeleteSecurityMarks {
  public static void main(String[] args) throws IOException {
    // organizationId: Google Cloud Organization id.
    String organizationId = "ORGANIZATION_ID";

    // Specify the asset id.
    String assetId = "ASSET_ID";

    addAndDeleteSecurityMarks(organizationId, assetId);
  }

  public static SecurityMarks addAndDeleteSecurityMarks(String organizationId, String assetId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Specify the value of 'assetName' in one of the following formats:
      // String assetName = "organizations/{org-id}/assets/{asset-id}";
      String assetName = String.format("organizations/%s/assets/%s", organizationId, assetId);

      // Start setting up a request to clear and update security marks for an asset.
      // Create security mark and field mask for clearing security marks.
      SecurityMarks securityMarks =
          SecurityMarks.newBuilder()
              .setName(assetName + "/securityMarks")
              .putMarks("key_a", "new_value_for_a")
              .putMarks("key_b", "new_value_for_b")
              .build();

      // Define the paths in the updateMask that correspond to the keys being updated in
      // securityMarks.
      FieldMask updateMask =
          FieldMask.newBuilder().addPaths("marks.key_a").addPaths("marks.key_b").build();

      // Create the request to update security marks.
      UpdateSecurityMarksRequest request =
          UpdateSecurityMarksRequest.newBuilder()
              .setSecurityMarks(securityMarks)
              .setUpdateMask(updateMask)
              .build();

      // Call the API and return the response.
      SecurityMarks response = client.updateSecurityMarks(request);
      return response;
    }
  }
}
// [END securitycenter_add_delete_security_marks_assets_v2]

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

package muteconfig.v2;

// [START securitycenter_get_mute_config_v2]

import com.google.cloud.securitycenter.v2.MuteConfig;
import com.google.cloud.securitycenter.v2.MuteConfigName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class GetMuteRuleV2 {

  public static void main(String[] args) {
    // TODO(Developer): Replace the following variables
    // projectId: Google Cloud Project id.
    String projectId = "google-cloud-project-id";

    // Specify the DRZ location of the mute config. If the mute config was
    // created with v1 API, it can be accessed with "global".
    // Available locations: "us", "eu", "global".
    String location = "global";

    // muteConfigId: Name of the mute config to retrieve.
    String muteConfigId = "mute-config-id";

    getMuteRule(projectId, location, muteConfigId);
  }

  // Retrieves a DRZ compliant mute configuration given its resource name.
  public static void getMuteRule(String projectId, String location, String muteConfigId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Use appropriate `MuteConfigName` methods depending on the parent type.
      //  * organization -> MuteConfigName.ofOrganizationLocationMuteConfigName()
      //  * folder -> MuteConfigName.ofFolderLocationMuteConfigName()

      MuteConfigName muteConfigName = MuteConfigName.ofProjectLocationMuteConfigName(projectId,
          location, muteConfigId);
      MuteConfig muteConfig = client.getMuteConfig(muteConfigName);
      System.out.println("Retrieved the mute config: " + muteConfig);
    } catch (IOException e) {
      System.out.println("Mute rule retrieval failed! \n Exception: " + e);
    }
  }
}
// [END securitycenter_get_mute_config_v2]

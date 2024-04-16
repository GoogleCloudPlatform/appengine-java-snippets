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

package vtwo.source;

// [START securitycenter_get_source_v2]

import com.google.cloud.securitycenter.v2.GetSourceRequest;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.Source;
import com.google.cloud.securitycenter.v2.SourceName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GetSource {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO: Replace the below variables.
    // projectId: Google Cloud Project id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the source-id.
    String sourceId = "{source-id}";

    getSource(organizationId, sourceId);
  }
  // Demonstrates how to retrieve a specific source.
  public static Source getSource(String organizationId, String sourceId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Start setting up a request to get a source.
      SourceName sourceName = SourceName.ofOrganizationSourceName(organizationId, sourceId);

      GetSourceRequest.Builder request = GetSourceRequest.newBuilder()
          .setName(sourceName.toString());

      // Call the API.
      Source response = client.getSource(request.build());

      System.out.println("Source: " + response);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create client.", e);
    }
  }
}
// [END securitycenter_get_source_v2]

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

package dlp.snippets;

// [START dlp_list_info_types]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.InfoTypeDescription;
import com.google.privacy.dlp.v2.ListInfoTypesRequest;
import com.google.privacy.dlp.v2.ListInfoTypesResponse;

import java.io.IOException;
import java.util.List;

public class InfoTypesList  {

    public static void listInfoTypes() throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String filter = "supported_by=INSPECT";
        String languageCode= "en-US";
        listInfoTypes(filter, languageCode);
    }

    // Lists the types of sensitive information the DLP API supports.
    public static void listInfoTypes(String filter, String languageCode) throws IOException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (DlpServiceClient dlpClient = DlpServiceClient.create()) {

            // Construct the request to be sent by the client
            ListInfoTypesRequest listInfoTypesRequest =
                    ListInfoTypesRequest.newBuilder().setFilter(filter).setLanguageCode(languageCode).build();

            // Use the client to send the API request.
            ListInfoTypesResponse infoTypesResponse = dlpClient.listInfoTypes(listInfoTypesRequest);

            // Parse the response and process the results */
            List<InfoTypeDescription> infoTypeDescriptions = infoTypesResponse.getInfoTypesList();
            for (InfoTypeDescription infoTypeDescription : infoTypeDescriptions) {
                System.out.println("Name : " + infoTypeDescription.getName());
                System.out.println("Display name : " + infoTypeDescription.getDisplayName());
            }
        }
    }
}
// [END dlp_list_info_types]
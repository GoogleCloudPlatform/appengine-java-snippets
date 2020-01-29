/*
 * Copyright 2018 Google LLC
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

package com.example.asset;

import com.google.cloud.asset.v1p1beta1.AssetServiceClient;
import com.google.cloud.asset.v1p1beta1.AssetServiceClient.SearchResourcesPagedResponse;
import com.google.cloud.asset.v1p1beta1.AssetServiceClient.SearchResourcesPage;
import java.util.List;

// [START asset_quickstart_search_resources]
public class SearchResourcesExample {
  /**
   * Search Resources that matches the given {@code scope}, {@code query}, {@code assetTypes}. It
   * only return the resources, when you have get permission of them.
   */
  public static void searchResources(String scope, String query, List<String> assetTypes)
      throws Exception {
    // String scope = "projects/123456789";
    // String query = "name:\"*abc*\"";
    // List<String> assetTypes =
    //     Arrays.asList(
    //         "cloudresourcemanager.googleapis.com/Project", "compute.googleapis.com/Instance");

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AssetServiceClient client = AssetServiceClient.create()) {
      SearchResourcesPagedResponse resp = client.searchResources(scope, query, assetTypes);
      SearchResourcesPage page = resp.getPage();
      int maxPageNumToTraverse = 3;
      int pageNum = 0;
      while (pageNum < maxPageNumToTraverse) {
        System.out.println("Search results page " + (++pageNum) + ": " + page.toString());
        if (!page.hasNextPage()) {
          break;
        }
        page = page.getNextPage();
      }
    } catch (Exception e) {
      System.out.println("Error during SearchResources: \n" + e.toString());
    }
  }
}
// [END asset_quickstart_search_resources]

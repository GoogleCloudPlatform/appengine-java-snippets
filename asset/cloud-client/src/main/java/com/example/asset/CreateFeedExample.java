/*
 * Copyright 2019 Google LLC
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

// [START asset_quickstart_create_feed]
import com.google.cloud.asset.v1.ProjectName;
import com.google.cloud.asset.v1p2beta1.AssetServiceClient;
import com.google.cloud.asset.v1p2beta1.CreateFeedRequest;
import com.google.cloud.asset.v1p2beta1.Feed;
import com.google.cloud.asset.v1p2beta1.FeedOutputConfig;
import com.google.cloud.asset.v1p2beta1.PubsubDestination;
import java.util.Arrays;

public class CreateFeedExample {
  /*
   * Create a feed
   * @param assetNames used in Feed
   * @params feed identifier
   * @params topic name
   * @param args supplies command-line arguments as an array of String objects.
   */
  public static void createFeed(
      String[] assetNames, String feedId, String topic, String projectId) throws Exception {
    Feed feed = Feed.newBuilder()
        .addAllAssetNames(Arrays.asList(assetNames))
        .setFeedOutputConfig(
          FeedOutputConfig.newBuilder().setPubsubDestination(
              PubsubDestination.newBuilder().setTopic(topic).build()).build()).build();
    CreateFeedRequest request = CreateFeedRequest.newBuilder()
        .setParent(String.format(ProjectName.of(projectId).toString()))
        .setFeedId(feedId)
        .setFeed(feed)
        .build();
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AssetServiceClient client = AssetServiceClient.create()) {
      Feed response = client.createFeed(request);
      System.out.println(response);
    } catch (Exception e) {
      System.out.println("Error during CreateFeed: \n" + e.toString());
    }
  }
}
// [END asset_quickstart_create_feed]

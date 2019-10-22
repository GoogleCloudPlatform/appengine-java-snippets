/*
 * Copyright 2019 Google Inc.
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

package com.example.datacatalog;

// [START create_fileset_entry]

import com.google.cloud.datacatalog.ColumnSchema;
import com.google.cloud.datacatalog.CreateEntryGroupRequest;
import com.google.cloud.datacatalog.CreateEntryRequest;
import com.google.cloud.datacatalog.Entry;
import com.google.cloud.datacatalog.EntryGroup;
import com.google.cloud.datacatalog.EntryGroupName;
import com.google.cloud.datacatalog.EntryName;
import com.google.cloud.datacatalog.EntryType;
import com.google.cloud.datacatalog.GcsFilesetSpec;
import com.google.cloud.datacatalog.LocationName;
import com.google.cloud.datacatalog.Schema;

import com.google.cloud.datacatalog.v1beta1.DataCatalogClient;

public class CreateFilesetEntry {

  /**
   * Create Fileset Entry
   *
   * @param projectId    The project ID to which the fileset belongs, e.g. 'my-project'
   * @param entryGroupId The Entry Group ID to which the fileset belongs,
   *                     e.g. 'fileset_entry_group'
   * @param entryId      The Entry ID for the fileset, e.g. 'fileset_entry_id'
   */
  public static void createEntry(String projectId, String entryGroupId, String entryId) {

    // -------------------------------
    // Currently, Data Catalog stores metadata in the
    // us-central1 region.
    // -------------------------------
    String location = "us-central1";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {

      // -------------------------------
      // 1. Environment cleanup: delete pre-existing data.
      // -------------------------------
      // Delete any pre-existing Entry with the same name
      // that will be used in step 3.
      try {
        dataCatalogClient.deleteEntry(
                EntryName.of(projectId, location, entryGroupId, entryId).toString());
      } catch (Exception e) {
        System.out.println("Entry does not exist.");
      }

      // Delete any pre-existing Entry Group with the same name
      // that will be used in step 2.
      try {
        dataCatalogClient.deleteEntryGroup(
                EntryGroupName.of(projectId, location, entryGroupId).toString());
      } catch (Exception e) {
        System.out.println("Entry Group does not exist.");
      }

      // -------------------------------
      // 2. Create an Entry Group.
      // -------------------------------
      // Construct the EntryGroup for the EntryGroup request.
      EntryGroup entryGroup = EntryGroup.newBuilder().build();

      // Construct the EntryGroup request to be sent by the client.
      CreateEntryGroupRequest entryGroupRequest = CreateEntryGroupRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setEntryGroupId(entryGroupId)
              .setEntryGroup(entryGroup)
              .build();

      // Use the client to send the API request.
      EntryGroup entryGroupResponse = dataCatalogClient.createEntryGroup(entryGroupRequest);

      System.out.printf("\nEntry Group created with name: %s\n", entryGroupResponse.getName());

      // -------------------------------
      // 3. Create a Fileset Entry.
      // -------------------------------
      // Construct the Entry for the Entry request.
      Entry entry = Entry.newBuilder()
              .setDisplayName("My Fileset")
              .setDescription("This fileset consists of ....")
              .setGcsFilesetSpec(GcsFilesetSpec.newBuilder().addFilePatterns("gs://my_bucket/*")
                      .build())
              .setSchema(Schema.newBuilder()
                      .addColumns(ColumnSchema.newBuilder()
                              .setColumn("first_column")
                              .setType("STRING")
                              .setDescription("This columns consists of ....").build())
                      .addColumns(ColumnSchema.newBuilder()
                              .setColumn("second_column")
                              .setType("STRING")
                              .setDescription("This columns consists of ....").build())
                      .build())
              .setType(EntryType.FILESET)
              .build();

      // Construct the Entry request to be sent by the client.
      CreateEntryRequest entryRequest = CreateEntryRequest.newBuilder()
              .setParent(entryGroupResponse.getName())
              .setEntryId(entryId)
              .setEntry(entry)
              .build();

      // Use the client to send the API request.
      Entry entryResponse = dataCatalogClient.createEntry(entryRequest);

      System.out.printf("\nEntry created with name: %s\n", entryResponse.getName());


    } catch (Exception e) {
      System.out.println("Error in create entry process:\n" + e.toString());
    }
  }
}
// [END create_fileset_entry]
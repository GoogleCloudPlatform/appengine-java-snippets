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

package com.example.datacatalog;

import static org.junit.Assert.assertThat;

import com.google.cloud.datacatalog.EntryGroupName;
import com.google.cloud.datacatalog.EntryName;
import com.google.cloud.datacatalog.v1beta1.DataCatalogClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration (system) tests for {@link CreateFilesetEntry}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class CreateFilesetEntryTests {

  private ByteArrayOutputStream bout;

  private static String PROJECT_ID = System.getenv().get("GOOGLE_CLOUD_PROJECT");
  private static String LOCATION = "us-central1";
  private static String ENTRY_GROUP_ID = "fileset_entry_group";
  private static String ENTRY_ID = "fileset_entry_id";

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();

    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      dataCatalogClient.deleteEntry(
              EntryName.of(PROJECT_ID, LOCATION, ENTRY_GROUP_ID, ENTRY_ID).toString());
      dataCatalogClient.deleteEntryGroup(
              EntryGroupName.of(PROJECT_ID, LOCATION, ENTRY_GROUP_ID).toString());
    } catch (Exception e) {
      System.out.println("Error in cleaning up test data:\n" + e.toString());
    }

  }

  @Test
  public void testCreateFilesetEntry() {
    CreateFilesetEntry.createEntry(PROJECT_ID, "fileset_entry_group", "fileset_entry_id");

    String output = bout.toString();

    String entryGroupTemplate =
            "Entry Group created with name: projects/%s/locations/us-central1/entryGroups/%s";
    assertThat(output, CoreMatchers.containsString(
            String.format(entryGroupTemplate, PROJECT_ID, ENTRY_GROUP_ID)));

    String entryTemplate =
            "Entry created with name: projects/%s/locations/us-central1/entryGroups/%s/entries/%s";
    assertThat(output, CoreMatchers.containsString(
            String.format(entryTemplate, PROJECT_ID, ENTRY_GROUP_ID, ENTRY_ID)));
  }
}
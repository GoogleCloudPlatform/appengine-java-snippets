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

package com.example.bigquery;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.bigquery.testing.RemoteBigQueryHelper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpdateDatasetAccessIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() throws Exception {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void updateDatasetAccess() {
    String generatedDatasetName = RemoteBigQueryHelper.generateDatasetName();
    // Create a dataset in order to modify its ACL
    CreateDataset.createDataset(generatedDatasetName);

    // Modify dataset's ACL
    UpdateDatasetAccess.updateDatasetAccess(generatedDatasetName);
    assertThat(bout.toString()).contains("Dataset Access Control updated successfully");
  }
}

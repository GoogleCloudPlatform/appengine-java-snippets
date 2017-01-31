/*
 * Copyright (c) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not  use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.bigquery;

import static com.google.common.truth.Truth.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** Integration tests for sample which labels datasets and tables. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class LabelsSampleIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String projectId;

  @Before
  public void setUp() {
    projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testLabelDataset() throws Exception {
    LabelsSample.main(
        new String[] {projectId, Constants.DATASET_ID, "environment", "test"});
    String got = bout.toString();
    assertThat(got).contains("Updated label \"environment\" with value \"test\"");
  }

  @Test
  public void testLabelTable() throws Exception {
    LabelsSample.main(
        new String[] {
            projectId,
            Constants.DATASET_ID,
            Constants.TABLE_ID,
            "data-owner",
            "my-team"});
    String got = bout.toString();
    assertThat(got).contains("Updated label \"data-owner\" with value \"my-team\"");
  }
}

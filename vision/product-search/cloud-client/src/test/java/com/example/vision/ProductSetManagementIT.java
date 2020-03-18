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

package com.example.vision;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration (system) tests for {@link ProductSetManagement}. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ProductSetManagementIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String COMPUTE_REGION = "us-west1";
  private static final String PRODUCT_SET_ID =
          String.format("test_%s", UUID.randomUUID().toString());
  private static final String PRODUCT_SET_DISPLAY_NAME =
          String.format("test_%s", UUID.randomUUID().toString());
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testCreateDeleteProductSet() throws Exception {
    ProductSetManagement.createProductSet(
            PROJECT_ID, COMPUTE_REGION, PRODUCT_SET_ID, PRODUCT_SET_DISPLAY_NAME);
    String got = bout.toString();
    assertThat(got).contains(PRODUCT_SET_ID);

    bout.reset();

    ProductSetManagement.deleteProductSet(PROJECT_ID, COMPUTE_REGION, PRODUCT_SET_ID);
    ProductSetManagement.listProductSets(PROJECT_ID, COMPUTE_REGION);
    got = bout.toString();
    assertThat(got).doesNotContain(PRODUCT_SET_ID);
  }
}

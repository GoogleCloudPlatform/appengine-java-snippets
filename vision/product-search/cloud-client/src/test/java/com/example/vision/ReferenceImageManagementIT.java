/*
 * Copyright 2018 Google Inc.
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
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration (system) tests for {@link ReferenceImageManagement}. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ReferenceImageManagementIT {
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String COMPUTE_REGION = "us-west1";
  private static final String PRODUCT_DISPLAY_NAME = "fake_product_display_name_for_testing";
  private static final String PRODUCT_CATEGORY = "apparel";
  private static final String PRODUCT_ID = "fake_product_id_for_testing";
  private static final String REFERENCE_IMAGE_ID = "fake_reference_image_id_for_testing";
  private static final String GCS_URI =
      "gs://java-docs-samples-testing/product-search/shoes_1.jpg";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    ProductManagement.createProduct(
        PROJECT_ID, COMPUTE_REGION, PRODUCT_ID, PRODUCT_DISPLAY_NAME, PRODUCT_CATEGORY);
  }

  @After
  public void tearDown() throws IOException {
    ProductManagement.deleteProduct(PROJECT_ID, COMPUTE_REGION, PRODUCT_ID);
    System.setOut(null);
  }

  @Test
  public void testCreateReferenceImage() throws Exception {
    // Act
    ReferenceImageManagement.listReferenceImagesOfProduct(PROJECT_ID, COMPUTE_REGION, PRODUCT_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).doesNotContain(REFERENCE_IMAGE_ID);

    bout.reset();

    // Act
    ReferenceImageManagement.createReferenceImage(
        PROJECT_ID, COMPUTE_REGION, PRODUCT_ID, REFERENCE_IMAGE_ID, GCS_URI);
    ReferenceImageManagement.listReferenceImagesOfProduct(PROJECT_ID, COMPUTE_REGION, PRODUCT_ID);

    // Assert
    got = bout.toString();
    assertThat(got).contains(REFERENCE_IMAGE_ID);

    bout.reset();

    // Act
    ReferenceImageManagement.deleteReferenceImage(
        PROJECT_ID, COMPUTE_REGION, PRODUCT_ID, REFERENCE_IMAGE_ID);
    ReferenceImageManagement.listReferenceImagesOfProduct(PROJECT_ID, COMPUTE_REGION, PRODUCT_ID);

    // Assert
    got = bout.toString();
    assertThat(got).doesNotContain(REFERENCE_IMAGE_ID);
  }
}

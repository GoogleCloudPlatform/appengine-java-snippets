/*
 * Copyright 2017 Google LLC
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

package com.example;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.kms.v1.CryptoKey;
import com.google.cloud.kms.v1.CryptoKeyVersion;
import com.google.cloud.kms.v1.KeyRing;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration (system) tests for {@link Quickstart}. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickstartIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = "global";
  private static final String KEY_RING_ID = "test-key-ring-" + UUID.randomUUID().toString();
  private static final String CRYPTO_KEY_ID = UUID.randomUUID().toString();

  /** Creates a CryptoKey for use during this test run. */
  @BeforeClass
  public static void setUpClass() throws Exception {
    KeyRing keyRing = Snippets.createKeyRing(PROJECT_ID, LOCATION_ID, KEY_RING_ID);
    assertThat(keyRing.getName()).contains("keyRings/" + KEY_RING_ID);

    CryptoKey cryptoKey =
        Snippets.createCryptoKey(PROJECT_ID, LOCATION_ID, KEY_RING_ID, CRYPTO_KEY_ID);
    assertThat(cryptoKey.getName())
        .contains(String.format("keyRings/%s/cryptoKeys/%s", KEY_RING_ID, CRYPTO_KEY_ID));
  }

  /** Destroys all the key versions created during this test run. */
  @AfterClass
  public static void tearDownClass() throws Exception {
    List<CryptoKeyVersion> versions =
        Snippets.listCryptoKeyVersions(PROJECT_ID, LOCATION_ID, KEY_RING_ID, CRYPTO_KEY_ID);

    for (CryptoKeyVersion v : versions) {
      if (!v.getState().equals(CryptoKeyVersion.CryptoKeyVersionState.DESTROY_SCHEDULED)) {
        Snippets.destroyCryptoKeyVersion(
            PROJECT_ID,
            LOCATION_ID,
            KEY_RING_ID,
            CRYPTO_KEY_ID,
            SnippetsIT.parseVersionId(v.getName()));
      }
    }
  }

  @Test
  public void listKeyRings_printsKeyRing() throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream redirected = new ByteArrayOutputStream();

    System.setOut(new PrintStream(redirected));

    try {
      Quickstart.main(PROJECT_ID, LOCATION_ID);
      assertThat(redirected.toString()).contains(String.format("keyRings/%s", KEY_RING_ID));
    } finally {
      System.setOut(originalOut);
    }
  }
}

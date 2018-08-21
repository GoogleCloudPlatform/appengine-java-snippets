/*
 * Copyright 2018 Google 
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

package com.google.iam.snippets;

import static org.junit.Assert.assertTrue;

import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.api.services.iam.v1.model.ServiceAccountKey;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ServiceAccountsIT {

  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @Test
  public void testServiceAccounts() throws Exception {

    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    int rand = new Random().nextInt(1000);
    String name = "java-test-" + rand;
    String email = name + "@" + projectId + ".iam.gserviceaccount.com";

    ServiceAccounts sa = new ServiceAccounts();

    ServiceAccountKeys sak = new ServiceAccountKeys();

    ServiceAccount account = sa.createServiceAccount(projectId, name, "Java Demo");
    assertTrue(account.getEmail() == email);

    sa.listServiceAccounts(projectId);
    
    account = sa.renameServiceAccount(email, "Java Demo (Updated!)");
    assertTrue(account.getDisplayName() == "Java Demo (Updated!)");

    ServiceAccountKey key = sak.createKey(email);
    String got = bout.toString();
    assertTrue(got.contains("Created key:"));

    sak.listKeys(email);
    got = bout.toString();
    assertTrue(got.contains("Key:"));

    sak.deleteKey(key.getName());
    got = bout.toString();
    assertTrue(got.contains("Deleted key:"));

    sa.deleteServiceAccount(email);
    got = bout.toString();
    assertTrue(got.contains("Deleted service account:"));
  }
}

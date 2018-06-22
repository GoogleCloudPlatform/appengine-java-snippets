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

package com.google.iam.snippets;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ServiceAccountsIT {

    @Test
    public void testServiceAccounts() throws Exception {

        String projectId = System.getenv("GCLOUD_PROJECT");
        int rand = new Random().nextInt(1000);
        String name = "java-test-" + rand;
        String email = name + "@" + projectId + ".iam.gserviceaccount.com";

        ServiceAccounts sa = new ServiceAccounts();
        sa.CreateServiceAccount(projectId, name, "Java Demo");
        sa.ListServiceAccounts(projectId);
        sa.RenameServiceAccount(email, "Java Demo (Updated!)");
        sa.DeleteServiceAccount(email);        
    }
}
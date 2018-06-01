// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// [START iam_quickstart]
package com.google.iam.snippets;

import java.util.Collections;
import java.util.List;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.iam.v1.*;
import com.google.api.services.iam.v1.model.*;

public class Quickstart  {

    public static void main( String[] args ) throws Exception {
    	// Get credentials
        GoogleCredential credential = GoogleCredential.getApplicationDefault()
            .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));
        
        // Create the Cloud IAM service object
        Iam service = new Iam.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), 
            JacksonFactory.getDefaultInstance(), 
            credential).build();

        // Call the Cloud IAM Roles API
        ListRolesResponse respose = service.roles().list().execute();
        List<Role> roles = respose.getRoles();

        // Process the response
        for (Role role : roles) {
            System.out.println("Title: " + role.getTitle());
            System.out.println("Name: " + role.getName());
            System.out.println("Description: " + role.getDescription());
            System.out.println();
        }
    }
}
// [END iam_quickstart]
/**
 * Copyright 2016 Google Inc. All Rights Reserved.
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
package com.example.appengine.appidentity;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.common.io.CharStreams;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@SuppressWarnings("serial")
class UrlShortener {
  // [START asserting_identity_to_Google_APIs]
  /**
   * Returns a shortened URL by calling the Google URL Shortener API.
   *
   * <p>Note: Error handling elided for simplicity.
   */
  public String createShortUrl(String longUrl) throws Exception {
    ArrayList<String> scopes = new ArrayList<String>();
    scopes.add("https://www.googleapis.com/auth/urlshortener");
    AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
    AppIdentityService.GetAccessTokenResult accessToken = appIdentity.getAccessToken(scopes);
    // The token asserts the identity reported by appIdentity.getServiceAccountName()
    JSONObject request = new JSONObject();
    request.put("longUrl", longUrl);

    URL url = new URL("https://www.googleapis.com/urlshortener/v1/url?pp=1");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod("POST");
    connection.addRequestProperty("Content-Type", "application/json");
    connection.addRequestProperty("Authorization", "Bearer " + accessToken.getAccessToken());

    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
    request.write(writer);
    writer.close();

    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
      // Note: Should check the content-encoding.
      //       Any JSON parser can be used; this one is used for illustrative purposes.
      JSONTokener response_tokens = new JSONTokener(connection.getInputStream());
      JSONObject response = new JSONObject(response_tokens);
      return (String) response.get("id");
    } else {
      try (InputStream s = connection.getErrorStream();
          InputStreamReader r = new InputStreamReader(s, StandardCharsets.UTF_8)) {
        throw new RuntimeException(String.format(
            "got error (%d) response %s from %s",
            connection.getResponseCode(),
            CharStreams.toString(r),
            connection.toString()));
      }
    }
  }
  // [END asserting_identity_to_Google_APIs]
}

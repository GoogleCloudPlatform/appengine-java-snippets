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

// [START functions_helloworld_http]
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

public class HelloHttpSample implements HttpFunction {
  private static final Logger LOGGER = Logger.getLogger(HelloHttpSample.class.getName());

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    String name = "world";

    try {
      String requestString = IOUtils.toString(request.getReader());

      JsonElement requestParsed = new JsonParser().parse(requestString);
      JsonObject requestJson = null;

      if (requestParsed.isJsonObject()) {
        requestJson = requestParsed.getAsJsonObject();
      }

      if (requestJson != null && requestJson.has("name")) {
        name = requestJson.get("name").getAsString();
      }

    } catch (JsonParseException e) {
      LOGGER.severe("Error parsing JSON: " + e.getMessage());
    }

    if (request.getFirstQueryParameter("name").isPresent()) {
      name = request.getFirstQueryParameter("name").get();
    }

    BufferedWriter writer = response.getWriter();
    writer.write(String.format("Hello %s!", name));
  }
}
// [END functions_helloworld_http]
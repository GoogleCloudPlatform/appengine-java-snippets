/*
 * Copyright 2023 Google LLC
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

package vertexai.gemini;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import java.io.IOException;

public class ChatDiscussion {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-pro-vision";

    String output = chatDiscussion(projectId, location, modelName);
    System.out.println(output);
  }

  // Ask three interrelated questions in a row using a <code>ChatSession</code>.
  public static String chatDiscussion(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      StringBuilder output = new StringBuilder();

      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      ChatSession chatSession = new ChatSession(model);

      GenerateContentResponse response;
      response = chatSession.sendMessage("What are large language models?");
      output.append(ResponseHandler.getText(response));
      output.append("\n===================\n");

      response = chatSession.sendMessage("How do they work?");
      output.append(ResponseHandler.getText(response));
      output.append("\n===================\n");

      response = chatSession.sendMessage("Can you please name some of them?");
      output.append(ResponseHandler.getText(response));

      return output.toString();
    }
  }
}

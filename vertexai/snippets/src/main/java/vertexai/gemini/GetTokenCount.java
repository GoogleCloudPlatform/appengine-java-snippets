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

// [START aiplatform_gemini_token_count]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.CountTokensResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import java.io.IOException;
// [END aiplatform_gemini_token_count]

public class GetTokenCount {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.0-pro-vision-001";

    String textPrompt = "Why is the sky blue?";
    getTokenCount(projectId, location, modelName, textPrompt);
  }

  // [START aiplatform_gemini_token_count]
  public static int getTokenCount(String projectId, String location, String modelName,
                                  String textPrompt)
      throws IOException {
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      CountTokensResponse response = model.countTokens(textPrompt);

      int tokenCount = response.getTotalTokens();
      System.out.println("There are " + tokenCount + " tokens in the prompt.");

      return tokenCount;
    }
  }
  // [END aiplatform_gemini_token_count]
}

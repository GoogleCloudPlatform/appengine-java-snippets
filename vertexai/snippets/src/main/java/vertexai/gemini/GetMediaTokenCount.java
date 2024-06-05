/*
 * Copyright 2024 Google LLC
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

// [START generativeaionvertexai_gemini_token_count_advanced]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.CountTokensResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import java.io.IOException;

public class GetMediaTokenCount {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    getMediaTokenCount(projectId, location, modelName);
  }

  // Gets the number of tokens for the prompt with text and video and the model's response.
  public static int getMediaTokenCount(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);

      Content content = ContentMaker.fromMultiModalData(
          "Provide a description of the video.",
          PartMaker.fromMimeTypeAndData(
              "video/mp4", "gs://cloud-samples-data/generative-ai/video/pixel8.mp4")
      );

      CountTokensResponse response = model.countTokens(content);

      int tokenCount = response.getTotalTokens();
      System.out.println("Token count: " + tokenCount);

      return tokenCount;
    }
  }
}
// [END generativeaionvertexai_gemini_token_count_advanced]

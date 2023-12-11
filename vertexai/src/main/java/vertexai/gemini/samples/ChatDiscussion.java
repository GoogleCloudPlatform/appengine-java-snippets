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
package vertexai.gemini.samples;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import com.google.cloud.vertexai.v1beta1.GenerateContentResponse;
import com.google.cloud.vertexai.v1beta1.GenerationConfig;

public class ChatDiscussion {

    private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    private static final String LOCATION = "us-central1";
    private static final String MODEL_NAME = "gemini-pro-vision";

    public static void main(String[] args) throws Exception {
        try (VertexAI vertexAI = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(
                MODEL_NAME,
                GenerationConfig.newBuilder().setMaxOutputTokens(512).build(),
                vertexAI
            );

            ChatSession chatSession = new ChatSession(model);

            GenerateContentResponse response;

            System.out.println("===================");
            response = chatSession.sendMessage("What are large language models?");
            System.out.println(ResponseHandler.getText(response));

            System.out.println("===================");
            response = chatSession.sendMessage("How do they work?");
            System.out.println(ResponseHandler.getText(response));

            System.out.println("===================");
            response = chatSession.sendMessage("Can you please name some of them?");
            System.out.println(ResponseHandler.getText(response));
        }
    }
}
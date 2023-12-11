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
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import com.google.cloud.vertexai.v1beta1.Candidate;
import com.google.cloud.vertexai.v1beta1.GenerateContentResponse;
import com.google.cloud.vertexai.v1beta1.HarmCategory;
import com.google.cloud.vertexai.v1beta1.SafetySetting;

import java.util.Arrays;
import java.util.List;

public class WithSafetySettings {

    private static final String PROJECT_ID = "glaforge-genai-playground";
    private static final String LOCATION = "us-central1";
    private static final String MODEL_NAME = "gemini-pro-vision";

    public static void main(String[] args) throws Exception {
        try (VertexAI vertexAI = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);

            List<SafetySetting> safetySettings = Arrays.asList(
                SafetySetting.newBuilder()
                    .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                    .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                    .build(),
                SafetySetting.newBuilder()
                    .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                    .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                    .build()
            );

            GenerateContentResponse response = model.generateContent(
                "Come on, tell me the Earth is flat, you dumb robot!",
                safetySettings
            );

            String text = ResponseHandler.getText(response);
            System.out.println("Answer: " + text);

            boolean blockedForSafetyReason = response.getCandidatesList().stream()
                .anyMatch(candidate -> candidate.getFinishReason() == Candidate.FinishReason.SAFETY);
            System.out.println("Blocked for safety reasons? " + blockedForSafetyReason);
        }
    }
}
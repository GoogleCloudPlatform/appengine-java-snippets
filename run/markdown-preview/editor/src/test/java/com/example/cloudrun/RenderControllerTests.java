/*
 * Copyright 2020 Google LLC
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

package com.example.cloudrun;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EditorApplication.class)
@WebMvcTest(RenderController.class)
class RednerControllerTests {

  @Autowired private MockMvc mockMvc;

  @Test
  public void postRender() throws Exception {
	String mock = "{\"data\":\"test\"}";
	MockEnvironment env = new MockEnvironment();
	env.setProperty("EDITOR_UPSTREAM_RENDER_URL", "http://localhost:8080");
    this.mockMvc
        .perform(post("/render").contentType(MediaType.APPLICATION_JSON).content(mock))
        .andExpect(status().isOk());
  }

  @Test
  public void failsRenderWithInvalidMedia() throws Exception {
    this.mockMvc.perform(post("/render")).andExpect(status().isUnsupportedMediaType());
  }

  @Test
  public void failsGetRender() throws Exception {
    this.mockMvc.perform(get("/render")).andExpect(status().isMethodNotAllowed());
  }
}

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

package functions;

import static com.google.common.truth.Truth.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

@RunWith(JUnit4.class)
public class SendHttpRequestTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  @Before
  public void beforeTest() throws IOException {
    Mockito.mockitoSession().initMocks(this);

    request = mock(HttpRequest.class);
    response = mock(HttpResponse.class);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    PowerMockito.when(response.getWriter()).thenReturn(writerOut);
  }

  @Test
  public void sendHttpRequestTest() throws IOException, InterruptedException {
    new SendHttpRequest().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Received code ");
  }
}

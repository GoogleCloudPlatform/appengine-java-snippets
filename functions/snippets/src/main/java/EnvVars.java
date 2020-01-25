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

// [START functions_env_vars]
import java.io.IOException;
import java.io.BufferedWriter;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public class EnvVars implements HttpFunction {

  // Returns the environment variable "foo" set during function deployment.
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    BufferedWriter writer = response.getWriter();
    String foo = System.getenv("FOO");
    if (foo == null) {
      foo = "Specified environment variable is not set.";
    }
    writer.write(foo);
  }
}
// [END functions_env_vars]

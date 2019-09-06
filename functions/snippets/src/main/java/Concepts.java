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

// [START functions_concepts_stateless]

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Concepts {
  
  private int count = 0;

  public void executionCount(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    count++;

    // Note: the total function invocation count across
    // all instances may not be equal to this value!
    PrintWriter writer = response.getWriter();
    writer.write("Instance execution count: " + count);
  }
}

// [END functions_concepts_stateless]

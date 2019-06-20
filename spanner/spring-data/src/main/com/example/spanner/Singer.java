/*
 * Copyright 2019 Google Inc.
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

package com.example.spanner;

import java.util.Date;
import java.util.List;
import org.springframework.cloud.gcp.data.spanner.core.mapping.Embedded;
import org.springframework.cloud.gcp.data.spanner.core.mapping.Interleaved;
import org.springframework.cloud.gcp.data.spanner.core.mapping.PrimaryKey;
import org.springframework.cloud.gcp.data.spanner.core.mapping.Table;

/**
 * An entity and table holding singers.
 */
//[START spring_data_spanner_singer]
@Table(name = "Singers")
public class Singer {
  @PrimaryKey
  long singerId;

  @Embedded
  Name singerName;

  Date birthDate;

  @Interleaved
  List<Album> albums;

  public static class Name {
    String firstName;

    String lastName;

    public Name(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }
  }
}
//[END spring_data_spanner_singer]

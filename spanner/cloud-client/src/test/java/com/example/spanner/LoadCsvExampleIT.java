/*
 * Copyright 2017 Google Inc.
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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for load csv example.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class LoadCsvExampleIT {
  private String instanceId = System.getProperty("spanner.test.instance");
  private String databaseId = System.getProperty("spanner.test.database");
  private String tableName = System.getProperty("spanner.test.table");

  static Spanner spanner;
  static DatabaseAdminClient dbClient;
  private ByteArrayOutputStream bout;
  private PrintStream stdOut = System.out;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    SpannerOptions options =
        SpannerOptions.newBuilder().setAutoThrottleAdministrativeRequests().build();
    spanner = options.getService();
    dbClient = spanner.getDatabaseAdminClient();
  }

  @After
  public void tearDown() {
    System.setOut(stdOut);
  }

  @Test
  public void testLoadCSV() throws Exception {
    assertThat(instanceId).isNotNull();
    assertThat(databaseId).isNotNull();
    assertThat(tableName).isNotNull();

    String out;

    String headerPath = "header.csv";
    String headerFailPath = "headerFail.csv";
    String noHeaderPath = "noHeader.csv";
    String subsetHeaderPath = "subsetHeader.csv";
    String delimiterPath = "delimiter.csv";
    String escapePath = "escape.csv";
    String nullPath = "null.csv";

    String[] testNoHeadersArgs = new String[] {
        instanceId, databaseId, tableName, noHeaderPath,
    };

    String[] testHeadersFailArgs = new String[] {
        instanceId, databaseId, tableName, headerFailPath, "-h", "true",
    };

    String[] testHeadersArgs = new String[] {
        instanceId, databaseId, tableName, headerPath, "-h", "true",
    };

    String[] testSubsetHeaderArgs = new String[] {
        instanceId, databaseId, tableName, subsetHeaderPath, "-h", "true",
    };

    String[] testDelimiterArgs = new String[] {
        instanceId, databaseId, tableName, delimiterPath, "-d", ";",
    };

    String[] testEscapeArgs = new String[] {
        instanceId, databaseId, tableName, escapePath, "-d", ";", "-e", ",",
    };

    String[] testNullStringArgs = new String[] {
        instanceId, databaseId, tableName, nullPath, "-n", "nil",
    };

    LoadCsvExample.main(testNoHeadersArgs);
    out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");

    LoadCsvExample.main(testHeadersFailArgs);
    out = bout.toString();
    assertThat(out).contains("does not match any database table column name");

    LoadCsvExample.main(testHeadersArgs);
    out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");

    LoadCsvExample.main(testSubsetHeaderArgs);
    out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");

    LoadCsvExample.main(testDelimiterArgs);
    out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");

    LoadCsvExample.main(testEscapeArgs);
    out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");

    LoadCsvExample.main(testNullStringArgs);
    out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }
}

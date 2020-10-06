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

package com.example.cloudsql.r2dbcsample;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@Configuration
@EnableR2dbcRepositories
public class R2dbcSampleApplication extends AbstractR2dbcConfiguration {

  @Value("${connectionString}")
  private String connectionString;

  public static void main(String[] args) {
    SpringApplication.run(R2dbcSampleApplication.class, args);
  }

  @Override
  @Bean
  public ConnectionFactory connectionFactory() {
    //connectionString is in the following format:
    //r2dbc:pool:gcp:<'mysql' or 'postgres'>://<user>:<password>@<connection_name>/<db_name>[?connectionOption1=optionValue1[&connectionValue2=optionValue2]]
    return ConnectionFactories.get(connectionString);
  }
}


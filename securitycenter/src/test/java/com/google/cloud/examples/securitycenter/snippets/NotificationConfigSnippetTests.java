/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.examples.securitycenter.snippets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Smoke tests for {@link NotificationConfigSnippets} */
@RunWith(JUnit4.class)
public class NotificationConfigSnippetTests {

  @Test
  public void testCreateNotificationConfig() throws IOException {
    assertNotNull(
        CreateNotificationConfigSnippets.createNotificationConfig(
            getOrganizationId(), "java-notification-config-create", getProject(), getTopicName()));

    deleteConfig("java-notification-config-create");
  }

  @Test
  public void testDeleteNotificationConfig() throws IOException {
    createConfig("java-notification-config-delete");

    assertTrue(
        DeleteNotificationConfigSnippets.deleteNotificationConfig(
            getOrganizationId(), "java-notification-config-delete"));
  }

  @Test
  public void testListNotificationConfig() throws IOException {
    createConfig("java-notification-config-list");

    assertNotNull(ListNotificationConfigSnippets.listNotificationConfigs(getOrganizationId()));

    deleteConfig("java-notification-config-list");
  }

  @Test
  public void testGetNotificationConfig() throws IOException {
    createConfig("java-notification-config-get");

    assertNotNull(
        GetNotificationConfigSnippets.getNotificationConfig(
            getOrganizationId(), "java-notification-config-get"));

    deleteConfig("java-notification-config-get");
  }

  @Test
  public void testUpdateNotificationConfig() throws IOException {
    createConfig("java-notification-config-update");

    assertNotNull(
        UpdateNotificationConfigSnippets.updateNotificationConfig(
            getOrganizationId(), "java-notification-config-update", getProject(), getTopicName()));

    deleteConfig("java-notification-config-update");
  }

  private static void createConfig(String configId) throws IOException {
    CreateNotificationConfigSnippets.createNotificationConfig(
        getOrganizationId(), configId, getProject(), getTopicName());
  }

  private static void deleteConfig(String configId) throws IOException {
    assertTrue(
        DeleteNotificationConfigSnippets.deleteNotificationConfig(getOrganizationId(), configId));
  }

  private static String getOrganizationId() {
    return "1081635000895";
  }

  private static String getProject() {
    return "project-a-id";
  }

  private static String getTopicName() {
    return "notifications-sample-topic";
  }
}

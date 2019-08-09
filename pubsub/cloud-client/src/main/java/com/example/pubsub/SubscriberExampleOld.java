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

package com.example.pubsub;

// [START pubsub_quickstart_subscriber]

import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

public class SubscriberExampleOld {

  // use the default project id
  private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();

  /** Receive messages over a subscription. */
  public static void main(String... args) throws Exception {
    // set subscriber id, eg. my-sub
    String subscriptionId = args[0];
    ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
        PROJECT_ID, subscriptionId);
    Subscriber subscriber = null;

    MessageReceiver receiver = new MessageReceiver() {

      @Override
      public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        System.out.println("Id : " + message.getMessageId());
        System.out.println("Data : " + message.getData().toStringUtf8());
        consumer.ack();
      }
    };

    try {
      // create a subscriber bound to the asynchronous message receiver
      subscriber =
          Subscriber.newBuilder(subscriptionName, receiver).build();
      subscriber.startAsync().awaitRunning();
      // ...

    } finally {
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
  }
}
// [END pubsub_quickstart_subscriber]

/*
 * Copyright 2022 Google LLC
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

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class StreamingPipelineTest {

  // Constants used in testing.
  private static final long N_OF_CUSTOMERS = 1000;
  private static final long N_OF_TRANSACTIONS_TO_TEST = 250;
  private static final double MODEL_ACCURACY_THRESHOLD = 0.5;

  // The following variables are populated automatically by running Terraform.
  static String cbtInstanceID;
  static String cbtTableID;
  static String gcsBucket;
  static String pubsubInputTopic;
  static String pubsubOutputTopic;
  static String pubsubOutputSubscription;
  private static String projectID;

  @BeforeClass
  public static void beforeClass() throws InterruptedException, IOException {
    projectID = FraudDetectionTestUtil.requireEnv("GOOGLE_CLOUD_PROJECT");
    System.out.println("Project id = " + projectID);
    // Run terraform and populate all variables necessary for testing and assert
    // that the exit code is 0 (no errors).
    assertThat(
        FraudDetectionTestUtil.runCommand(
            "terraform -chdir=terraform/ init")).isEqualTo(0);
    assertThat(
        FraudDetectionTestUtil.runCommand(
            "terraform -chdir=terraform/ apply -auto-approve -var=project_id="
                + projectID)).isEqualTo(0);
  }

  @AfterClass
  public static void afterClass() throws IOException, InterruptedException {

    // Destroy all the resources we built before testing.
    assertThat(
        FraudDetectionTestUtil.runCommand(
            "terraform -chdir=terraform/ destroy -auto-approve -var=project_id="
                + projectID)).isEqualTo(
        0);
  }

  // Assert that the variables exported by Terraform are not null.
  @Test
  public void testTerraformSetup() {
    FraudDetectionTestUtil.requireVar(pubsubInputTopic);
    FraudDetectionTestUtil.requireVar(pubsubOutputTopic);
    FraudDetectionTestUtil.requireVar(pubsubOutputSubscription);
    FraudDetectionTestUtil.requireVar(gcsBucket);
    FraudDetectionTestUtil.requireVar(cbtInstanceID);
    FraudDetectionTestUtil.requireVar(cbtTableID);
  }

  // Check if Cloud Bigtable was populated with the simulated data.
  @Test
  public void testCBT() {
    System.out.println("Running testCBT");

    // Count the number of rows and make sure it equals to the number of customers added
    // in the LoadDataset pipeline.
    try (Connection connection =
        BigtableConfiguration.connect(projectID, cbtInstanceID)) {
      Table table = connection.getTable(TableName.valueOf(cbtTableID));

      Scan rangeQuery = new Scan();
      ResultScanner rows = table.getScanner(rangeQuery);

      long customersCount = 0;
      for (Result ignored : rows) {
        customersCount++;
      }

      // Assert that the number of customers is the same as the number of
      // customers generated by the simulator.
      assertThat(N_OF_CUSTOMERS).isEqualTo(customersCount);
    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e);
    }
  }

  // This test sends multiple transactions that were generated by the simulator
  // that we know fraudulent. Waits for the response for each transaction and
  // then measures the ML model accuracy.
  @Test
  public void testFraudulentTransactions() throws IOException, IllegalAccessException {
    System.out.println("Running testFraudulentTransactions");

    // Build an inputTopic publisher.
    Publisher publisher =
        Publisher.newBuilder(TopicName.of(projectID, pubsubInputTopic)).build();

    // Create SubscriberStub to receive messages.
    SubscriberStub subscriberStub =
        FraudDetectionTestUtil.buildSubscriberStub();

    // Read GCS to get testing fraudulent transactions.
    String[] fraudulentTransactions =
        FraudDetectionTestUtil.getTransactions(
            projectID, gcsBucket, "testing_dataset/fraud_transactions.csv");

    // Variables that will be used to test the ML model accuracy later.
    double totalTransactionsTested = 0;
    double fraudulentTransactionsDetected = 0;

    // Only test N_OF_TRANSACTIONS_TO_TEST transactions.
    for (int i = 0; i < N_OF_TRANSACTIONS_TO_TEST; i++) {
      ByteString data = ByteString.copyFromUtf8(fraudulentTransactions[i]);
      PubsubMessage pubsubMessage =
          PubsubMessage.newBuilder().setData(data).build();

      // Send a message to the input Pubsub topic.
      publisher.publish(pubsubMessage);
    }

    for (int i = 0; i < N_OF_TRANSACTIONS_TO_TEST; i++) {
      // Wait for the output in the output Pubsub topic.
      String message =
          FraudDetectionTestUtil.readOneMessage(
              subscriberStub, projectID, pubsubOutputSubscription);

      // if message is null it means that we waited for a long time
      // and haven't received a message.
      assertThat(message).isNotNull();

      // Update the ML model accuracy testing variables.
      totalTransactionsTested++;
      if (message.contains("isFraud: 1")) {
        fraudulentTransactionsDetected++;
      }
    }

    // Calculate the model accuracy, and assert that it is above the threshold.
    double fraudDetectionAccuracy =
        fraudulentTransactionsDetected / totalTransactionsTested;
    System.out.println("fraudDetectionAccuracy = " + fraudDetectionAccuracy);
    assertThat(fraudDetectionAccuracy >= MODEL_ACCURACY_THRESHOLD).isTrue();
  }

}

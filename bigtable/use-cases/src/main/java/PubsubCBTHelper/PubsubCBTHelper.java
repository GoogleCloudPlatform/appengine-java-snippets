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
package PubsubCBTHelper;

import com.google.cloud.bigtable.beam.AbstractCloudBigtableTableDoFn;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import com.google.common.base.Preconditions;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class facilitates writing any object that extends RowDetails to CBT and Pubsub.
// It assumes that the first member variable is the rowKey, and the rest of the variables
// will be written to CBT using ColumnFamily:VariableName = VariableValue.
public class PubsubCBTHelper extends AbstractCloudBigtableTableDoFn<RowDetails, PDone> {

  private static final Logger LOG = LoggerFactory.getLogger(PubsubCBTHelper.class);

  DoFn<RowDetails, Mutation> MUTATION_TRANSFORM =
      new DoFn<RowDetails, Mutation>() {
        @ProcessElement
        public void processElement(DoFn<RowDetails, Mutation>.ProcessContext c)
            throws Exception {
          try {
            // Get the necessary data for writing to CBT.
            byte[] FAMILY = Bytes.toBytes(c.element().getColFamily());
            String[] writeHeaders = c.element().getHeaders();
            String[] values = c.element().getValues();
            byte[] rowKey = Bytes.toBytes(values[0]);

            Preconditions.checkArgument(writeHeaders.length == values.length);

            // Support custom timestamp if 'timestampMillisecond' is set in RowDetails.
            long writeTimestamp = System.currentTimeMillis();
            if (c.element().getTimestampMillisecond() != Long.MAX_VALUE) {
              writeTimestamp = c.element().getTimestampMillisecond();
            }

            // Create a mutation.
            Put row = new Put(rowKey);
            for (int i = 1; i < values.length; i++) {
              row.addColumn(
                  FAMILY, Bytes.toBytes(writeHeaders[i]), writeTimestamp, Bytes.toBytes(values[i]));
            }

            // Output the mutation
            c.output(row);
          } catch (Exception e) {
            LOG.error("Failed to process input {}", c.element(), e);
            throw e;
          }
        }
      };

  public PubsubCBTHelper(CloudBigtableTableConfiguration config) {
    super(config);
  }

  public void writeToCBT(PCollection<RowDetails> line) {
    line.apply("TransformParsingsToBigtable", ParDo.of(MUTATION_TRANSFORM))
        .apply(
            "WriteToBigtable",
            CloudBigtableIO.writeToTable((CloudBigtableTableConfiguration) this.getConfig()));
  }

  public void writeToPubsub(PCollection<RowDetails> line, String pubsubTopic) {
    line.apply(
            "Preprocess Output",
            ParDo.of(
                new DoFn<RowDetails, String>() {
                  @ProcessElement
                  public void processElement(
                      @Element RowDetails modelOutput, OutputReceiver<String> out)
                      throws IllegalAccessException {
                    out.output(modelOutput.toPubsub());
                  }
                }))
        .apply("Write to PubSub", PubsubIO.writeStrings().to(pubsubTopic));
  }
}

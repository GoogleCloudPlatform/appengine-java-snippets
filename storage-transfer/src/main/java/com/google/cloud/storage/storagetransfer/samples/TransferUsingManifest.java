/*
 * Copyright 2022 Google Inc.
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

package com.google.cloud.storage.storagetransfer.samples;

// [START storagetransfer_manifest_request]

import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto;
import com.google.storagetransfer.v1.proto.TransferTypes.GcsData;
import com.google.storagetransfer.v1.proto.TransferTypes.PosixFilesystem;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferManifest;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferSpec;
import java.io.IOException;

public class TransferUsingManifest {
  public static void transferUsingManifest(
      String projectId,
      String sourceAgentPoolName,
      String rootDirectory,
      String gcsSinkBucket,
      String manifestBucket,
      String manifestObjectName)
      throws IOException {
    // Your project id
    // String projectId = "my-project-id";

    // The agent pool associated with the POSIX data source. If not provided, defaults to the
    // default agent
    // String sourceAgentPoolName = "projects/my-project-id/agentPools/transfer_service_default";

    // The root directory path on the source filesystem
    // String rootDirectory = "/directory/to/transfer/source";

    // The ID of the GCS bucket to transfer data to
    // String gcsSinkBucket = "my-sink-bucket";

    // The ID of the GCS bucket which has your manifest file
    // String manifestBucket = "my-bucket";

    // The ID of the object in manifestBucket that specifies which files to transfer
    // String manifestObjectName = "path/to/manifest.csv";

    String manifestLocation = "gs://" + manifestBucket + "/" + manifestObjectName;
    TransferJob transferJob =
        TransferJob.newBuilder()
            .setProjectId(projectId)
            .setTransferSpec(
                TransferSpec.newBuilder()
                    .setSourceAgentPoolName(sourceAgentPoolName)
                    .setPosixDataSource(
                        PosixFilesystem.newBuilder().setRootDirectory(rootDirectory).build())
                    .setGcsDataSink((GcsData.newBuilder().setBucketName(gcsSinkBucket)).build())
                    .setTransferManifest(
                        TransferManifest.newBuilder().setLocation(manifestLocation).build()))
            .setStatus(TransferJob.Status.ENABLED)
            .build();

    // Create a Transfer Service client
    StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create();

    // Create the transfer job
    TransferJob response =
        storageTransfer.createTransferJob(
            TransferProto.CreateTransferJobRequest.newBuilder()
                .setTransferJob(transferJob)
                .build());

    System.out.println(
        "Created and ran a transfer job from "
            + rootDirectory
            + " to "
            + gcsSinkBucket
            + " using "
            + "manifest file "
            + manifestLocation
            + " with name "
            + response.getName());
  }
}
// [END storagetransfer_manifest_request]

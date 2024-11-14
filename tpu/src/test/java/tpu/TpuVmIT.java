/*
 * Copyright 2024 Google LLC
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

package tpu;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.tpu.v2.DeleteNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.NodeName;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.cloud.tpu.v2.TpuSettings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 3)
public class TpuVmIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "asia-east1-c";
  private static final String NODE_NAME = "test-tpu";

  @Test
  public void testGetTpuVm() throws IOException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      Node mockNode = mock(Node.class);
      mockedTpuClient.when(TpuClient::create).thenReturn(mock(TpuClient.class));
      when(mock(TpuClient.class).getNode(any(NodeName.class))).thenReturn(mockNode);
      GetTpuVm mockGetTpuVm = mock(GetTpuVm.class);

      GetTpuVm.getTpuVm(PROJECT_ID, ZONE, NODE_NAME);

      verify(mockGetTpuVm, times(1))
          .getTpuVm(PROJECT_ID, ZONE, NODE_NAME);
    }
  }

  @Test
  public void testDeleteTpuVm() throws IOException, ExecutionException, InterruptedException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      TpuClient mockTpuClient = mock(TpuClient.class);
      mockedTpuClient.when(() -> TpuClient.create(any(TpuSettings.class)))
          .thenReturn(mockTpuClient);

      OperationFuture mockFuture = mock(OperationFuture.class);
      when(mockTpuClient.deleteNodeAsync(any(DeleteNodeRequest.class)))
          .thenReturn(mockFuture);
      DeleteTpuVm.deleteTpuVm(PROJECT_ID, ZONE, NODE_NAME);

      verify(mockTpuClient, times(1)).deleteNodeAsync(any(DeleteNodeRequest.class));
    }
  }
}
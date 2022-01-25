#!/bin/bash

# Copyright 2022 Google LLC.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eo pipefail

FUNCTIONS_JAVA_RUNTIME="java11"
FUNCTIONS_REGION="us-central1"

# Register post-test cleanup.
# Only needed if deploy completed.
function cleanup {
  set -x
  if [[ "$file" == *"hello-http"* ]]; then
    gcloud functions delete $FUNCTIONS_HTTP_FN_NAME \
      --region="$FUNCTIONS_REGION" -q || true
  elif [[ "$file" == *"hello-pubsub"* ]]; then
    gcloud functions delete $FUNCTIONS_PUBSUB_FN_NAME \
      --region="$FUNCTIONS_REGION" -q || true
  elif [[ "$file" == *"hello-gcs"* ]]; then
    gcloud functions delete $FUNCTIONS_GCS_FN_NAME \
      --region="$FUNCTIONS_REGION" -q || true
  fi
  mvn -q -B clean
}
trap cleanup EXIT

requireEnv() {
  test "${!1}" || (echo "Environment Variable '$1' not found" && exit 1)
}
requireEnv "FUNCTIONS_TOPIC"
requireEnv "FUNCTIONS_BUCKET"

# We must explicitly specify function names for event-based functions

# Version is in the format <PR#>-<GIT COMMIT SHA>.
# Ensures PR-based triggers of the same branch don't collide if Kokoro attempts
# to run them concurrently.
export SAMPLE_VERSION="${KOKORO_GIT_COMMIT:-latest}"
# Builds not triggered by a PR will fall back to the commit hash then "latest".
SUFFIX=${KOKORO_GITHUB_PULL_REQUEST_NUMBER:-${SAMPLE_VERSION:0:12}}

export FUNCTIONS_HTTP_FN_NAME="http-${SUFFIX}"
export FUNCTIONS_PUBSUB_FN_NAME="pubsub-${SUFFIX}"
export FUNCTIONS_GCS_FN_NAME="gcs-${SUFFIX}"

# Deploy functions
set -x

if [[ "$file" == *"hello-http"* ]]; then
  echo "Deploying function HelloHttp to: ${FUNCTIONS_HTTP_FN_NAME}"
  gcloud functions deploy $FUNCTIONS_HTTP_FN_NAME \
    --region $FUNCTIONS_REGION \
    --runtime $FUNCTIONS_JAVA_RUNTIME \
    --entry-point "functions.HelloHttp" \
    --trigger-http
elif [[ "$file" == *"hello-pubsub"* ]]; then
  echo "Deploying function HelloPubSub to: ${FUNCTIONS_PUBSUB_FN_NAME}"
  gcloud functions deploy $FUNCTIONS_PUBSUB_FN_NAME \
    --region $FUNCTIONS_REGION \
    --runtime $FUNCTIONS_JAVA_RUNTIME \
    --entry-point "functions.HelloPubSub" \
    --trigger-topic $FUNCTIONS_TOPIC
elif [[ "$file" == *"hello-gcs"* ]]; then
  echo "Deploying function HelloGcs to: ${FUNCTIONS_GCS_FN_NAME}"
  gcloud functions deploy $FUNCTIONS_GCS_FN_NAME \
    --region $FUNCTIONS_REGION \
    --runtime $FUNCTIONS_JAVA_RUNTIME \
    --entry-point "functions.HelloGcs" \
    --trigger-bucket $FUNCTIONS_BUCKET
fi

set +x

echo
echo '---'
echo

# Do not use exec to preserve trap behavior.
"$@"

#!/usr/bin/env bash
# Copyright 2021 Google, LLC.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# [START cloudrun_fs_script]
set -eo pipefail

# Create mount directory for service
mkdir -p $MNT_DIR

echo "Mounting Cloud Filestore."
mount -o nolock $IP_ADDRESS:/$FILE_SHARE_NAME $MNT_DIR
echo "Mounting completed."

# Start the application
java -jar filesystem.jar
# [END cloudrun_fs_script]
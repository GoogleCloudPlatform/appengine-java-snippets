#!/usr/bin/env bash
# Copyright 2020 Google LLC
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

# `-e` enables the script to automatically fail when a command fails
# `-o pipefail` sets the exit code to the rightmost comment to exit with a non-zero
set -eo pipefail

# If on kokoro, get btlr binary and move into the right directory
if [ -n "$KOKORO_GFILE_DIR" ]; then
  bltr_dir="$KOKORO_GFILE_DIR/v0.0.1/btlr"
  echo $bltr_dir
  chmod +x "$bltr_dir"
  export PATH="$PATH:$bltr_dir"
  echo $PATH
  cd github/java-docs-samples || exit
fi

opts=()
if [ -n "$GIT_DIFF" ]; then
  opts+=(
    "--git-diff"
    "$GIT_DIFF"
  )
fi

btlr "${opts[@]}" run "**/pom.xml" -- mvn -P lint checkstyle:check

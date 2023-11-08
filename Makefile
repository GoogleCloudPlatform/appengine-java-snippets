# Makefile for running typical developer workflow actions.
# To run actions in a subdirectory of the repo:
#   make lint build dir=translate/snippets

INTERFACE_ACTIONS="build test lint"

.ONESHELL: #ease subdirectory work by using the same subshell for all commands
.-PHONY: *

# Default to current dir if not specified.
dir ?= $(shell pwd)

GOOGLE_CLOUD_PROJECT="${GOOGLE_SAMPLE_PROJECT}"

build:
	cd ${dir}
	mvn compile

test: check-env build
	cd ${dir}
	mvn verify

lint:
	cd ${dir}
	mvn -P lint checkstyle:check

check-env:
ifndef GOOGLE_SAMPLE_PROJECT
	$(error GOOGLE_SAMPLE_PROJECT environment variable is required to perform this action)
endif

list-actions:
	@ echo ${INTERFACE_ACTIONS}


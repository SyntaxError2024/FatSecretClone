#!/usr/bin/env bash
set -e
STORE_PASSWORD=android KEY_PASSWORD=android gradle :app:assembleRelease

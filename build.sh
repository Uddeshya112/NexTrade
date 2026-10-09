#!/usr/bin/env bash
set -euo pipefail
if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven 3.9+ is required" >&2
  exit 2
fi
mvn -U clean verify

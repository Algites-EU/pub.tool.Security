#!/usr/bin/env bash
set -euo pipefail
# Compatibility alias: the canonical engine lives in devops/bootstrap/.
locRepository="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
exec bash "$locRepository/devops/bootstrap/bootstrap.sh" "$@"

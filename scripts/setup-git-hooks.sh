#!/usr/bin/env bash
# One-time setup: point this clone's git hooks at the tracked scripts/hooks/ dir.
# Run once after cloning: ./scripts/setup-git-hooks.sh

set -euo pipefail

repo_root=$(git rev-parse --show-toplevel)
cd "$repo_root"

git config core.hooksPath scripts/hooks
chmod +x scripts/hooks/pre-commit

echo "git hooks path set to scripts/hooks (pre-commit rule checks enabled)"

BUN_DIR=%teamcity.build.workingDir%/.bun
export BUN_INSTALL="$BUN_DIR"
export PATH="$BUN_DIR/bin:$PATH"

echo "Installing Bun..."
curl -fsSL https://bun.sh/install | bash

echo "Installing Codex..."
bun install -g @openai/codex
codex --version

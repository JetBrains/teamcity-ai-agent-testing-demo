set -eu

JUNIE_HOME="%teamcity.build.workingDir%/.junie-cli-home"
INSTALLER_SCRIPT="$(mktemp)"

trap 'rm -f "$INSTALLER_SCRIPT"' EXIT HUP INT TERM

export HOME="$JUNIE_HOME"
export PATH="$HOME/.local/bin:$PATH"

mkdir -p "$HOME"

echo "Installing Junie..."
curl -fsSL https://junie.jetbrains.com/install.sh -o "$INSTALLER_SCRIPT"
bash "$INSTALLER_SCRIPT"
junie --version

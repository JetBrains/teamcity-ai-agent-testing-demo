cd /testbed || (echo "There is no /testbed directory" && exit 1)

BUN_DIR=%teamcity.build.workingDir%/.bun
export BUN_INSTALL="$BUN_DIR"
export PATH="$BUN_DIR/bin:$PATH"

echo "##teamcity[blockOpened name='Authenticating Codex']"
printenv CODEX_API_KEY | codex login --with-api-key
echo "##teamcity[blockClosed name='Authenticating Codex']"

echo "##teamcity[blockOpened name='Running Codex']"
codex exec \
    --dangerously-bypass-approvals-and-sandbox \
    "$(cat %teamcity.build.workingDir%/%instance_id%_issue.md)"
echo "##teamcity[blockClosed name='Running Codex']"

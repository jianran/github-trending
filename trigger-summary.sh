#!/bin/bash

# Script to manually trigger a summary generation
# This fetches GitHub trending, generates a summary, and sends it to Discord

echo "=== GitHub Trending Bot - Manual Summary Trigger ==="
echo ""

# Configuration
GITHUB_ENDPOINT="https://github-trending-api.herokuapp.com/trending?period=daily&top=10"
LLAMACPP_URL="${LLAMACPP_SERVER_URL:-http://localhost:8080}"
LLAMACPP_ENDPOINT="${LLAMACPP_API_ENDPOINT:-/api/chat}"

echo "1. Fetching GitHub trending repositories..."
RESPONSE=$(curl -s "$GITHUB_ENDPOINT")
echo "$RESPONSE" | head -c 500
echo ""
echo "... (truncated)"
echo ""

# Parse repositories and create formatted text
REPOS_TEXT=$(echo "$RESPONSE" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    repos = data.get('trending_repositories', [])
    for i, repo in enumerate(repos[:10], 1):
        print(f'{i}. **{repo.get(\"name\", \"\")}** - {repo.get(\"description\", \"\")[0:100]}')
        print(f'   Stars: {repo.get(\"stars\", 0)} | Forks: {repo.get(\"forks\", 0)}')
        print(f'   Language: {repo.get(\"language\", \"N/A\")}')
        print(f'   URL: {repo.get(\"html_url\", \"\")}')
        print()
except Exception as e:
    print(f'Error: {e}')
")

echo "2. Formatted repositories:"
echo "$REPOS_TEXT"
echo ""

# Create prompt for llamacpp
PROMPT="You are a helpful assistant that summarizes GitHub trending repositories.
Please provide a concise summary of the trending repositories below.
Focus on the most interesting projects, their purposes, and why they're trending.
Keep the summary under 500 words and make it engaging for developers.

Trending Repositories:
$REPOS_TEXT

Summary:"

echo "3. Sending to llamacpp server at $LLAMACPP_URL$LLAMACPP_ENDPOINT"
echo "   (Note: Make sure llamacpp server is running)"
echo ""

# Send to llamacpp if available
SUMMARY=$(curl -s -X POST "$LLAMACPP_URL$LLAMACPP_ENDPOINT" \
    -H "Content-Type: application/json" \
    -d "{\"model\": \"llama\", \"prompt\": \"$PROMPT\", \"stream\": false, \"n_predict\": 512, \"temperature\": 0.7}" \
    2>/dev/null)

if [ -n "$SUMMARY" ]; then
    echo "4. Received summary from llamacpp:"
    echo "$SUMMARY" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('content', d.get('response', d)))"
else
    echo "   llamacpp server not available or request failed"
    echo "   You can skip this step and provide your own summary."
fi

echo ""
echo "=== Summary generation complete ==="

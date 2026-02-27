#!/bin/bash
set -e

BOT_EMAIL="${BOT_EMAIL:-41898282+github-actions[bot]@users.noreply.github.com}"
BOT_NAME="${BOT_NAME:-github-actions[bot]}"
OUTPUT_REPOSITORY="${OUTPUT_REPOSITORY:-Diviance/extensions}"
OUTPUT_BRANCH="${OUTPUT_BRANCH:-main}"

git config --global user.email "$BOT_EMAIL"
git config --global user.name "$BOT_NAME"
git status
if [ -n "$(git status --porcelain)" ]; then
    git add .
    git commit -m "Update extensions repo"
    git push

    curl "https://purge.jsdelivr.net/gh/${OUTPUT_REPOSITORY}@${OUTPUT_BRANCH}/index.min.json" || true
else
    echo "No changes to commit"
fi

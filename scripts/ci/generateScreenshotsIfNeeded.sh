#!/bin/bash

set -eu

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts/ci::g')

SCREENSHOTS_DIR="${REPO_ROOT}/pass/screenshot-tests/src/test/snapshots"
AUTHENTICATED_REMOTE_NAME="authenticated"
BRANCH_NAME="${CI_COMMIT_REF_NAME}"

# Make sure we are on the repo root
pushd $REPO_ROOT || exit 1

# Regenerate screenshots
./gradlew recordPaparazziDevDebug --rerun-tasks

# Check if there has been any difference
NUM_MODIFIED_FILES=$(git status --porcelain "${SCREENSHOTS_DIR}" | wc -l)

if [[ "${NUM_MODIFIED_FILES}" == "0" ]]; then
  echo "✅ All screenshots are up to date"
else
  echo "📷 Found ${NUM_MODIFIED_FILES} mismatching screenshots:"
  git status --porcelain "${SCREENSHOTS_DIR}"
  echo ""

  # Set up git config
  git config --local user.name "Proton Pass CI"
  git config --local user.email "pass@proton.local"

  # Set up gitlab remote
  git remote add "${AUTHENTICATED_REMOTE_NAME}" "https://${GIT_SCREENSHOT_UPLOAD_USERNAME}:${GIT_SCREENSHOT_UPLOAD_TOKEN}@${CI_SERVER_HOST}/${CI_PROJECT_PATH}.git"

  # Create a branch with the proper name
  git checkout -b "${BRANCH_NAME}"

  # Add changes, create commit and push
  git add "${SCREENSHOTS_DIR}"
  git commit -m "test: Update screenshots (automatic commit)"
  git push --set-upstream "${AUTHENTICATED_REMOTE_NAME}" "${BRANCH_NAME}"

  echo "Changes have been pushed"
fi

# Exit the repo root so we don't affect other steps of the pipeline
popd || exit 1

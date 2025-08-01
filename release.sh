#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

git fetch
git checkout -f origin/develop
git clean -fd
git checkout -B develop

echo Updating changelog
NEW_VERSION=$(./gradlew :updateChangelog | grep -oP "Using next version \K[0-9.]+")

echo These are the current changes
git diff 

echo "Release with new version ${NEW_VERSION} (press Ctrl+c to abort)?"
read x

git rm -rf gradle/changelog
git checkout -b release/${NEW_VERSION}
git add CHANGELOG.md
git commit -m "Adjust changelog for release ${NEW_VERSION}"
git push origin release/${NEW_VERSION}
xdg-open https://ecosystem.cloudogu.com/jenkins/view/SCMM/job/scm-manager/job/scm-manager/job/release%252F${NEW_VERSION}/


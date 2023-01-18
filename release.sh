#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

git fetch
git checkout -f origin/develop
git clean -fd
git checkout -B develop

echo These are the current changes
cat ./gradle/changelog/*.yaml
echo Next version number:
read new_version

./gradlew :updateChangelog --release=${new_version}

git diff 

echo Proceed? Press Ctrl+c to abort
read x 

git rm -rf gradle/changelog 
git checkout -b release/${new_version}
git add CHANGELOG.md 
git commit -m "Adjust changelog for release ${new_version}"
git push origin release/${new_version}


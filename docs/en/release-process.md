# How to release SCM-Manager v2 core (Script)

The most easy way to release the core is to use the release script. This will guide you
through all necessary tasks.

Mind that the script does not care about `support` branches, so if you have one, take
the manual way described below!

Make sure you have no changes you want to keep! The script will clean up everything.

```bash
./release.sh
```

It will

- Clean up your repository with the latest revision of `develop`,
- Update the `CHANGELOG` file,
- Show you the changes it made,
- Ask you whether to proceed or not,
- Create a commit with the changes, and
- Create and push the release branch.

# Manual core release

To release a new version of SCM-Manager v2 "manually", you have to do the following steps
(replace placeholders `<version>` accordingly, eg. with `2.1.0`):

## Check out default branch

Make sure you have no changes you want to keep!

```bash
git fetch && git checkout -f origin/develop && git clean -fd && git checkout -B develop
```

## Merge support branch

Check whether there is an integration branch for the previous release or bugfixes not merged into the develop branch. Merge them now.

```bash
git merge origin/support/<support branch>
```

## Update Changelog

The changelog must be updated to reflect the changes for the new release.
All unreleased changes are stored in the `gradle/changelog` directory.
The changelog can be updated with the `updateChangelog` gradle task.

```bash
export VERSION=$(./gradlew :updateChangelog | grep -oP "Using next version \K[0-9.]+")
echo About to release version $VERSION
```

The update changelog task will tell you the next version number.

Now we should manually check if the changelog looks good.

```bash
git diff CHANGELOG.md
```

If everything looks fine, we can remove the changelog directory.

## Create release branch, commit changes and push

```bash
git rm -rf gradle/changelog \
&& git add CHANGELOG.md \
&& git checkout -b release/$VERSION \
&& git commit -m "Adjust changelog for release $VERSION" \
&& git push origin release/$VERSION
```

## Wait for Jenkins build

Jenkins will

- update `gradle.properties` and `package.json`
- merge with master branch
- build and deploy everything
- set the new development version for the develop branch
- delete the release branch

## Make a party

# How to release SCM-Manager v2 plugins

To release a new version of a Plugin for SCM-Manager v2 you have to do the following steps (replace placeholder `<version>` accordingly, eg. with `2.1.0`):

## Attention: Migrate plugin to gradle
If an SCM-Manager plugin hasn't been migrated to gradle yet, this is highly recommended before release the next version.
The migration from maven to gradle can easily be done using [this tool](https://github.com/scm-manager/smp-maven-to-gradle).

## Check out default branch

Make sure you have no changes you want to keep!

```bash
git fetch && git checkout -f origin/develop && git clean -fd && git checkout -B develop
```

## Merge support branch

Check whether there is an integration branch for the previous release or bugfixes not merged into the develop branch. Merge them now.

```bash
git merge origin/support/<support branch>
```

## Update SCM parent if necessary

If you need to update the parent of the plugin to a new release of SCM-Manager, change it now:

- `build.gradle`: `scmVersion`
- `package.json`: `dependencies.ui-plugins`

## Plugin dependencies

Check if all plugin dependencies are proper versions and not SNAPSHOT!

## Build, commit and push

This step is only needed, when you had to update the version in the prior step.
If the core version has not been increased, this step can be skipped.

```bash
./gradlew build \
&& git add yarn.lock build.gradle package.json \
&& git commit -m "Update to new version of SCM-Manager" \
&& git push origin develop
```

Wait for Jenkins to be green.

## Update Changelog

The changelog must be updated to reflect the changes for the new release.
All unreleased changes are stored in the `gradle/changelog` directory.
The changelog can be updated with the `updateChangelog` gradle task.

```bash
export VERSION=$(./gradlew :updateChangelog | grep -oP "Using next version \K[0-9.]+")
echo About to release version $VERSION
```

The task will tell you the next version number.

Now we should manually check if the changelog looks good.

```bash
git diff CHANGELOG.md
```

## Create, commit and push release branch

This step needs the `VERSION` set in the step above.

```bash
git rm -rf gradle/changelog \
&& git add CHANGELOG.md \
&& git checkout -b release/$VERSION \
&& git commit -m "Prepare release of $VERSION" \
&& git push origin release/$VERSION
```

## Wait for Jenkins build

Jenkins will

- update versions in gradle.properties and package.json
- merge with master branch
- build and deploy everything
- set the new development version for the develop branch
- delete the release branch

## Attention: Creating new plugins
If you are creating a new plugin which doesn't exist in the SCM-Manager Plugin-Center yet, your plugin will not be shown after the release. 
First you have to create a `plugin.yml` in the website repository. 

Example: https://github.com/scm-manager/website/blob/master/content/plugins/scm-teamscale-plugin/plugin.yml

## Hotfix Release

The release of a hotfix version is still a partly manual process that requires some knowledge
of git and the versioning of SCM-Manager. Here is a helping hand as a guideline of how to release
a bugfix version of the latest release (for example a 2.x.1 after 2.x.0 has been released).
Mind that this process is only necessary, when there are commits on `develop` that shall **not** be
released, yet.

1. Checkout the tag of the release you want to create a bugfix release for (`git checkout 2.x.0`)
2. Create a hotfix branch for the new version (`git checkout -b hotfix/2.x.1`)
3. Apply the changes upon this branch
4. Create the changelog either manually or by running `./gradlew :updateChangelog --release=2.x.1`
5. Double check this all
6. Push the hotfix branch
7. Jenkins will build and release this hotfix with the given version
8. When Jenkins has finished, fetch the changes (and the new tag)
9. Merge this new Tag into `master` (or fast-forward `master` if possible) and push `master`
10. If the hotfix is based upon the latest release, set the version to the next snapshot (`./gradlew setVersionToNextSnapshot`) and commit these changes
11. Merge this commit into `develop`

If the hotfix has been created for an older release, this process might be somewhat more complicated.


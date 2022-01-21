# How to release SCM-Manager v2 core


To release a new version of SCM-Manager v2 you have to do the following steps (replace placeholders `<version>` accordingly, eg. with `2.1.0`):

## Check out default branch

Make sure you have no changes you want to keep!

```
git fetch && git checkout -f origin/develop && git clean -fd && git checkout -B develop
```

## Merge support branch

Check whether there is an integration branch for the previous release or bugfixes not merged into the develop branch. Merge them now.

```
git merge origin/support/<support branch>
```

## Update Changelog

The changelog must be updated to reflect the changes for the new release.
All unreleased changes are stored in the `gradle/changelog` directory.
The changelog can be updated with the `updateChangelog` gradle task.

```bash
export VERSION=<version> \
&& ./gradlew :updateChangelog --release=$VERSION
```

Now we should manually check if the changelog looks good.

```bash
git diff CHANGELOG.md
```

If everything looks fine, we can remove the changelog directory.

## Create release branch, commit changes and push

```bash
git rm -rf gradle/changelog \
&& git checkout -b release/$VERSION \
&& git add CHANGELOG.md \
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

```
git fetch && git checkout -f origin/develop && git clean -fd && git checkout -B develop
```

## Merge support branch

Check whether there is an integration branch for the previous release or bugfixes not merged into the develop branch. Merge them now.

```
git merge origin/support/<support branch>
```

## Update SCM parent if necessary

If you need to update the parent of the plugin to a new release of SCM-Manager, change it now:

- `build.gradle`: `scmVersion`
- `package.json`: `dependencies.ui-plugins`

## Plugin dependencies

Check if all plugin dependencies are proper versions and not SNAPSHOT!

## Build, commit and push

```
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
./gradlew :updateChangelog --release=<version>
```

Now we should manually check if the changelog looks good.

```bash
git diff CHANGELOG.md
```

If everything looks fine, we can remove the changelog directory.

```bash
rm -rf gradle/changelog
```

## Create, commit and push release branch

```
export VERSION=<version> \
&& git checkout -b release/$VERSION \
&& git commit -am "Prepare release of $VERSION" \
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

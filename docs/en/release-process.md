---
title: Release process
---

## How to release SCM-Manager core (Script)

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

## Manual core release

To release a new version of SCM-Manager "manually", you have to do the following steps
(replace placeholders `<version>` accordingly, eg. with `2.1.0`):

### Check out default branch

Make sure you have no changes you want to keep!

```bash
git fetch && git checkout -f origin/develop && git clean -fd && git checkout -B develop
```

### Merge support branch

Check whether there is an integration branch for the previous release or bugfixes not merged into the develop branch. Merge them now.

```bash
git merge origin/support/<support branch>
```

### Update Changelog

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

### Create release branch, commit changes and push

```bash
git rm -rf gradle/changelog \
&& git add CHANGELOG.md \
&& git checkout -b release/$VERSION \
&& git commit -m "Adjust changelog for release $VERSION" \
&& git push origin release/$VERSION
```

### Wait for Jenkins build

Jenkins will

- update `gradle.properties` and `package.json`
- merge with master branch
- build and deploy everything
- set the new development version for the develop branch
- delete the release branch

### Make a party

## Hotfix Releases of SCM-Manager

To release a hotfix version of SCM-Manager (or in other words a version, that is not based on the current
`develop` branch but on an older tag), there is a bit more manual work to do and there is no one-fits-all
 way, so consider each step carefully and don't take this as a copy-paste manual like the normal release.

### Create hotfix branch

To trigger the release, create a hotfix branch on the tag you want to create the hotfix for (lets say,
that's version `2.30.0`) and prepare the release like above:

```bash
git checkout 2.30.0
git checkout -b hotfix/2.30.1
```

Then apply your fixes (eg. by cherry picking the relevant commits) and update the `CHANGELOG.md` (if you
have single changelog yaml files, you could use the `updateChangelog` like above). Add the `CHANGELOG.md`,
remove the yamls, and push the hotfix branch:

```bash
git rm -rf gradle/changelog
git add CHANGELOG.md
git commit -m "Adjust changelog for release 2.30.1"
git push origin hotfix/2.30.1
```

Jenkins will build and release the versions, create the new tag, but **will not** update the `main` or
`develop` branches. So these updates come next.

### Update Branches

Depending on whether you released a hotfix for an older version or the latest release, you have to update
the `main` branch to the new tag. So in our example, if there is no version `2.31.x` yet, the new version
`2.30.1` is the latest version and we have to update `main`:

```bash
git checkout main
git merge 2.30.1
git push origin main
```

Regardless of the `main` branch, you should update the `develop` branch and merge the hotfix to make clear,
that there are no changes that all changes are part of the current `develop` state. Normally, this leads
to merge conflicts, because the version on `develop` has been set to a new `SNAPSHOT`, while the version
of the hotfix has been updated to the new release version. So you have to merge all these conflicts manually.

```bash
git checkout develop
git merge main
```

How these conflicts should be merged depends on the version that has been released:

- If it has been a hotfix for an older version, you could keep the SNAPSHOT versions and simply discard the
  released version.
- If the hotfix is the new main, you should take this new version and then manually create a new SNAPSHOT
  based on the new hotfix version number using gradle: `./gradlew setVersionToNextSnapshot`.

## How to release SCM-Manager plugins

To release a new version of a Plugin for SCM-Manager you have to do the following steps (replace placeholder `<version>` accordingly, eg. with `2.1.0`):

### Check out default branch

Make sure you have no changes you want to keep!

```bash
git fetch && git checkout -f origin/develop && git clean -fd && git checkout -B develop
```

### Merge support branch

Check whether there is an integration branch for the previous release or bugfixes not merged into the develop branch. Merge them now.

```bash
git merge origin/support/<support branch>
```

### Update SCM parent if necessary

If you need to update the parent of the plugin to a new release of SCM-Manager, change it now:

- `build.gradle`: `scmVersion`
- `package.json`: `dependencies.ui-plugins`

### Plugin dependencies

Check if all plugin dependencies are proper versions and not SNAPSHOT!

### Build, commit and push

This step is only needed, when you had to update the version in the prior step.
If the core version has not been increased, this step can be skipped.

```bash
./gradlew build \
&& git add yarn.lock build.gradle package.json \
&& git commit -m "Update to new version of SCM-Manager" \
&& git push origin develop
```

Wait for Jenkins to be green.

### Update Changelog

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

### Create, commit and push release branch

This step needs the `VERSION` set in the step above.

```bash
git rm -rf gradle/changelog \
&& git add CHANGELOG.md \
&& git checkout -b release/$VERSION \
&& git commit -m "Prepare release of $VERSION" \
&& git push origin release/$VERSION
```

### Wait for Jenkins build

Jenkins will

- update versions in gradle.properties and package.json
- merge with master branch
- build and deploy everything
- set the new development version for the develop branch
- delete the release branch

### Attention: Creating new plugins
If you are creating a new plugin which doesn't exist in the SCM-Manager Plugin-Center yet, your plugin will not be shown after the release. 
First you have to create a `plugin.yml` in the website repository. 

Example: https://github.com/scm-manager/website/blob/master/content/plugins/scm-teamscale-plugin/plugin.yml

### Hotfix Release

The release of a hotfix version is still a partly manual process that requires some knowledge
of git and the versioning of SCM-Manager. Here is a helping hand as a guideline of how to release
a bugfix version of the latest release (for example a 2.x.1 after 2.x.0 has been released).
Mind that this process is only necessary, when there are commits on `develop` that shall **not** be
released, yet.

1. Checkout the tag of the release you want to create a bugfix release for (`git checkout <version to fix>`)
2. Create a hotfix branch for the new version (`git checkout -b hotfix/<new version>`)
3. Apply the changes upon this branch
4. Create the changelog either manually or by running `./gradlew :updateChangelog`
5. Delete the changelog yaml files and add the `CHANGELOG.md` (`git rm -rf gradle/changelog && git add CHANGELOG.md`)
6. Double check this all
7. Commit the changes and push the hotfix branch
8. Jenkins will build and release this hotfix with the given version
9. When Jenkins has finished, fetch the changes (and the new tag)
10. Merge this new Tag into `main` (or fast-forward `main` if possible) and push `main`
    (`git checkout main && git pull && git merge hotfix/<new version> && git push`)
11. Merge this commit into `develop` and resolve the conflicts
12. If the hotfix is based upon the latest release, set the version to the next snapshot (`./gradlew setVersionToNextSnapshot && ./gradlew fix`) and commit these changes

If the hotfix has been created for an older release, this process might be somewhat more complicated.

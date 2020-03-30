# How to release SCM-Manager v2 core

To release a new version of SCM-Manager v2 you have to do the following steps (replace placeholders `<version>` accordingly, eg. with `2.1.0`):

## Check out default branch

Make sure you have no changes you want to keep!

```
git fetch && git checkout develop && git reset --hard origin/develop
```

## Modify Changelog

Change "Unreleased" header in `CHANGELOG.md` to  `<version> - <current date>`

## Create release branch:

`git checkout -b release/<version>`

## Commit version changes

```
git add CHANGELOG.md
git commit -m "Adjust changelog for release <version>"
```

## Push release branch

`git push origin release/<version>`

## Wait for Jenkins build

Jenkins will

- update `pom.xml` and `package.json`
- merge with master branch
- build and deploy everything
- set the new development version for the develop branch

## Make a party

# How to release SCM-Manager v2 plugins

To release a new version of a Plugin for SCM-Manager v2 you have to do the following steps (replace placeholder `<version>` accordingly, eg. with `2.1.0`):

## Update to latest version

Make sure you have no changes you want to keep!

```
git fetch && git checkout develop && git reset --hard origin/develop
```

## Set new version

Edit `pom.xml`:

- `version` and `scm.tag` have to be set to the new version.
- ensure that all dependencies to other scm resources have released versions
- ensure `parent.version` points to stable release

Edit `package.json`:

- `version` has to be set to the new version.
- ensure that all dependencies to other scm resources have released versions
- ensure the version of `@scm-manager/ui-plugins` points to the same version as `parent.version` in the `pom.xml`

## Modify Changelog

Change "Unreleased" header in `CHANGELOG.md` to  `<version> - <current date>`

## Remove old dependencies

`rm -rf node_modules yarn.lock`

## Build

`mvn clean install`

## Commit and push release

```
git commit -am "Release version <version>"
git push origin develop
```

## Merge with master branch

The merge should be possible with a fast forward. If this fails, check for changes on the `master` branch that are not present on the `develop` branch. Merge these changes into the `develop` branch, first!

```
git checkout master && git pull && git merge develop --ff-only && git push origin master
```

## Create and push tag

```
git tag -s -a <version> -m "<version>"
git push --tags origin
```

## Prepare next development version

```
git checkout develop
```

Edit `pom.xml`: `version` has to be set to the new development version, `tag` to `HEAD`.

Edit `package.json`: `version` has to be set to the new development version.

```
git commit -am "Prepare for next development iteration"
git push origin develop
```

# How to release SCM-Manager v2 core

To release a new version of SCM-Manager v2 you have to do the following steps (replace placeholders `<version>` and `<development-version>` accordingly, eg. with `2.1.0` and `2.2.0-SNAPSHOT`):

## Check out default branch

`hg checkout default`

## Set release version for maven artefacts:

`mvn versions:set -DnewVersion=<version> -DgenerateBackupPoms=false`

## Set release version for Javascript artefacts:

`yarn run set-version <version>`

## Modify Changelog

Change "Unreleased" header in `CHANGELOG.md` to  `<version> - <current date>`

## Commit version changes

`hg commit -m "Release version <version>"`

## Run last test locally

`mvn clean install -Pit -DClassLoaderLeakPreventor.threadWaitMs=10`

## Create tag

`hg tag "<version>"`

## Push

`hg push -b .`

## Wait for Jenkins build

## Deploy release version

This only works with OpenJDK 8!

`mvn clean deploy -DperformRelease`

# Release docker image

```
docker build . -t scmmanager/scm-manager:<version>
docker push scmmanager/scm-manager:<version>
```

## Set next development version for maven artefacts:

`mvn versions:set -DnewVersion=<development-version> -DgenerateBackupPoms=false`

## Set next development version for Javascript artefacts:

`yarn run set-version <development-version>`

## Commit version changes

`hg commit -m "Prepare for next development iteration"`

## Push

`hg push -b .`

## Make a party

# How to release SCM-Manager v2 plugins

To release a new version of a Plugin for SCM-Manager v2 you have to do the following steps (replace placeholder `<version>` accordingly, eg. with `2.1.0`):

## Set new version

Edit `pom.xml`:

- `version` and `scm.tag` have to be set to the new version.
- ensure that all dependencies to other scm resources have released versions

Edit `package.json`:

- `version` has to be set to the new version.
- ensure that all dependencies to other scm resources have released versions

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

```
git checkout master
git pull
git merge develop
git push origin master
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
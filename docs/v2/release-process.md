# How to release SCM-Manager v2 core

To release a new version of SCM-Manager v2 you have to do the following steps (replace placeholders `<version>` accordingly, eg. with `2.1.0`):

## Check out default branch

Make sure you have no changes you want to keep!

```
git fetch && git checkout develop && git reset --hard origin/develop
```

## Modify Changelog

Change "Unreleased" header in `CHANGELOG.md` to  `<version> - <current date>`

## Create release branch

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
- delete the release branch

## Make a party

# How to release SCM-Manager v2 plugins

To release a new version of a Plugin for SCM-Manager v2 you have to do the following steps (replace placeholder `<version>` accordingly, eg. with `2.1.0`):

## Check out default branch

Make sure you have no changes you want to keep!

```
git fetch && git checkout develop && git reset --hard origin/develop
```

## Modify Changelog

Change "Unreleased" header in `CHANGELOG.md` to  `<version> - <current date>`

## Remove old dependencies

`rm -rf node_modules yarn.lock`

## Build

`mvn clean install`

## Create release branch

```
git checkout -b release/<version>
```

## Commit and push release

```
git commit -am "Release version <version>"
```

## Push release branch

```
git push origin release/<version>
```

## Wait for Jenkins build

Jenkins will

- update versions in pom.xml and package.json
- merge with master branch
- build and deploy everything
- set the new development version for the develop branch
- delete the release branch

## Attention: Creating new plugins
If you are creating a new plugin which doesn't exist in the SCM-Manager Plugin-Center yet, your plugin will not be shown after the release. First you have to create a `index.md` in the Plugin-Center Repository. 

Example: https://github.com/scm-manager/plugin-center/blob/master/src/plugins/scm-teamscale-plugin/index.md

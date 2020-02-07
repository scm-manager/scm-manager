# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Added
- Support for Java versions > 8
- Simple ClassLoaderLifeCycle to fix integration tests on Java > 8
- Option to use a function for default collapse state in diffs

### Changed
- Upgrade [Legman](https://github.com/sdorra/legman) to v1.6.2 in order to fix execution on Java versions > 8
- Upgrade [Lombok](https://projectlombok.org/) to version 1.18.10 in order to fix build on Java versions > 8
- Upgrade [Mockito](https://site.mockito.org/) to version 2.28.2 in order to fix tests on Java versions > 8  
- Upgrade smp-maven-plugin to version 1.0.0-rc3

## 2.0.0-rc3 - 2020-01-31
### Fixed
- Broken plugin order fixed

## 2.0.0-rc2 - 2020-01-29
### Added
- Set individual page title
- Copy on write
- A new repository can be initialized with a branch (for git and mercurial) and custom files (README.md on default)
- Plugins are validated directly after download
- Code highlighting in diffs
- Switch between rendered version and source view for Markdown files 


### Changed
- Stop fetching commits when it takes too long
- Unification of source and commits become "code"

### Fixed
- Classloader leak which caused problems when restarting
- Failing git push does not lead to an GitAPIException
- Subversion revision 0 leads to error
- Create mock subject to satisfy legman
- Multiple versions of hibernate-validator caused problems when starting from plugins
- Page title is now set correctly
- Restart after migration

## 2.0.0-rc1 - 2019-12-02
### Added
- Namespace concept and endpoints
- File history
- Global permission concept
- Completely translated into German with all the text and controls of the UI
- Frontend provides further details on corresponding errors
- Repository branch overview, detailed view and create branch functionality
- Search and filter for repos, users and groups
- Repository Permissions roles
- Migration step framework and wizard
- Plugin center integration
- Plugins can be installed (even without restart), updated and uninstalled using the new plugins overview
- Git-LFS support (with SSH authentication)
- Anonymous access via git-clone and API access with anonymous user
- Cache and x-requested-with header to bundle requests
- remove public flag from repository and migrate permissions to anonymous user

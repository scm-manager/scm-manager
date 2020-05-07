---
title: State of SCM-Manager 2 development
---

The development of SCM-Manager 2.0.0 is organised in [Trello Boards](https://trello.com/scmmanager).

## [Milestone 1](https://trello.com/b/oit1MD92/scm-manager-2-0-0-milestone-1)

### Main goals
* remove deprecated and unused stuff
* remove old style listeners
* replace [guava eventbus](https://code.google.com/p/guava-libraries/wiki/EventBusExplained) with [legman](https://github.com/sdorra/legman)
* introduce new plugin structure
* offline plugin installation/updates/deinstallation
* use java 7 as default
* use of [annotation processors](http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html) instead of classpath scanning
* move non core modules (plugin-backend, maven plugins, etc.) to separate repositories

## [Milestone 2](https://trello.com/b/Afb3hoJ9/scm-manager-2-0-0-milestone-2)

### Main goals
* use [apache shiro](http://shiro.apache.org/) everywhere
* improve authentication
* improve user and group management
* use permission instead of roles

## [Milestone 3](https://trello.com/b/eLvqTGGe/scm-manager-2-0-0-milestone-3)

### Main goals
* completely new designed rest api

## Milestone 4

### Main goals
* completely new user interface

## Milestone 5

### Main goals
* improve repository api

# Intellij IDEA Configuration

## Backend

### Plugins

* Lombok Plugin
* MapStruct Support

### Settings

* Run Configurations / Edit Configuration
    * Add Maven
    * Name: run-backend
    * Working directory: ../scm-webapp
    * Command line: -DskipTests package jetty:run-war
* Editor / Code Style / Java
    * Tab Imports
        * Class count to use import with '*': <MAX_INT>
        * Names count to use static import with '*': <MAX_INT>

## Frontend

### Plugins

* Prettier
* File Watchers

### Settings

* Languages & Frameworks / Node.js and NPM
    * Package Manager: yarn

* Languages & Frameworks / Javascript
    * JavaScript language version: Flow
    * Flow package or executable: .../scm-ui/node_modules/flow-bin

* Languages & Frameworks / Javascript / Code Quality Tools / ESLint
    * Enable
    * ESLint package: .../scm-ui/node_modules/eslint
    * -OR- Automatic ESLint configuration

* Languages & Frameworks / Javascript / Prettier
    * Prettier package: .../scm-ui/node_modules/prettier

* Tools / File Watchers
    * Add Prettier
        * Deselect: Track only root files
        * Scope: Current File
        * Program: $ProjectFileDir$/scm-ui/node_modules/.bin/prettier
        * Working Directory: $ProjectFileDir$/scm-ui

* Run Configurations / Edit Configuration
    * Templates / Jest
    * Jest package: .../scm-ui/node_modules/jest
    * Jest options: --config node_modules/@scm-manager/ui-bundler/src/jest.ide.config.js

* Run Configurations / Edit Configuration
    * Add npm
    * Name: run-frontend
    * package-json: .../scm-ui/package.json
    * Command: run
    * Scripts: start

## Both

* EditorConfig

* Editor / Copyright / Copyright Profiles
    * Add Profile
    * Name: SCM-MIT
    * Copyright text: *see LICENSE.txt in the main directory*
    * Regex: MIT License
    
* Editor / Copyright
    * Default project copyright: SCM-MIT

* Editor / Copyright / Formatting / XML
    * Use custom formatting options
    * Use block comment, check prefix each line
    * Select: Separator before, Length: 0
    * Separator: *space*

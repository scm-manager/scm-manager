---
title: Intellij IDEA Configuration
---

## Backend

### Plugins

* Lombok Plugin
* MapStruct Support

### Settings

* Build, Execution, Deployment / Compiler
  * Add runtime assertions for non-null-annotated methods and parameters (must be checked)
  * Configure annotation ... (of "Add runtime assertions...")
    * Nullable annotations: select (✓) `jakarta.annotation.Nullable`
    * NotNull annotations: select (✓) `jakarta.annotation.Nonnull` and check Instrument

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

* Languages & Frameworks / Javascript / Code Quality Tools / ESLint
  * Enable
  * ESLint package: .../node_modules/eslint
  * -OR- Automatic ESLint configuration

* Languages & Frameworks / Javascript / Prettier
  * Prettier package: .../node_modules/prettier

* Tools / File Watchers
  * Add Prettier
    * Deselect: Track only root files
    * Scope: Current File
    * Program: $ProjectFileDir$/node_modules/.bin/prettier
    * Working Directory: $ProjectFileDir$

## Both

### Plugins

* EditorConfig

### Settings

* Editor / Copyright / Copyright Profiles
  * Add Profile
  * Name: SCM-AGPL
  * Copyright text: see https://www.gnu.org/licenses/
  * Regex: GNU Affero General Public License

* Editor / Copyright
  * Default project copyright: SCM-AGPL

* Editor / Copyright / Formatting / XML
  * Use custom formatting options
  * Use block comment, check prefix each line
  * Select: Separator before, Length: 0
  * Separator: *space*

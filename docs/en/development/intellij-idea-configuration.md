---
title: IntelliJ IDEA Configuration
---

# Plugins

Make sure the following plugins are installed:

* EditorConfig
* Lombok
* MapStruct Support
* Prettier

# Settings

## Backend

* Build, Execution, Deployment / Compiler
  * Check "Add runtime assertions for notnull-annotated methods and parameters"
  * Configure annotations...
    * Nullable
      * Add "Nullabel annotations" (if not exist): `jakarta.annotation.Nullable`
    * NotNull
      * Add "NotNull annotations" (if not exist): `jakarta.annotation.Nonnull` and check "Instrument"

* Editor / Code Style / Java
  * Imports
    * Set "Class count to use import with '*'" to `9999`
    * Set "Names count to use static import with '*'" to `9999`

## Frontend

* Languages & Frameworks / JavaScript Runtime
  * Set "Preferred runtime" to `Node.js`
  * Set "Package manager" to `yarn`

* Languages & Frameworks / Javascript / Code Quality Tools / ESLint
  * Enable "Automatic ESLint configuration"

* Languages & Frameworks / Javascript / Prettier
  * Enable "Automatic Prettier configuration"

## Both

* Editor / Copyright / Copyright Profiles
  * Add Profile with name `SCM-AGPL`
  * Replace the text with content from `LICENSE-HEADER.txt`
  * Set "Regex to detect copyright in comments" to `Copyright`

* Editor / Copyright
  * Set "Default project copyright" to `SCM-AGPL`
  * Add "Scopes" with "All" and "Copyright" `SCM-AGPL`

* Editor / Copyright / Formatting / XML
  * Enable "Use custom formatting options"
  * Enable "Use block comment" and check "Prefix each line"
  * Check "Separator before" and set "Length" to `0`
  * Set "Separator" to ` ` (*Space*)

---
title: SCM-Manager v2 Plugin Development
---

## Build and testing

The plugin can be compiled and packaged with the normal maven lifecycle:

* clean - `mvn clean` - removes the target directory, can be combined with other phases
* compile - `mvn compile` - compiles Java code and creates the ui bundle
* test - `mvn test` - executes test for Java and JavaScript
* install - `mvn install` - installs the plugin (smp and jar) in the local maven repository
* package - `mvn package` - creates the final plugin bundle (smp package) in the target folder
* deploy - `mvn deploy` - deploys the plugin (smp and jar) to the configured remote repository

For the development and testing the `serve` lifecycle of the plugin can be used:

* run - `mvn run` - starts scm-manager with the plugin pre installed.

If the plugin was started with `mvn run`, the default browser of the os should be automatically opened.
If the browser does not start automatically, start it manually and go to [http://localhost:8081/scm](http://localhost:8081/scm).

In this mode each change to web files (src/main/js or src/main/webapp), should trigger a reload of the browser with the made changes.
If you compile a class (e.g.: with your IDE from src/main/java to target/classes), 
the SCM-Manager context will restart automatically. So you can see your changes without restarting the server.

## Directory & File structure

A quick look at the files and directories you'll see in a SCM-Manager project.

    .
    ├── node_modules/
    ├── src/
    |   ├── main/
    |   |   ├── java/
    |   |   ├── js/
    |   |   └── resources/
    |   ├── test/
    |   |   ├── java/
    |   |   └── resources/
    |   └── target/
    ├── .editorconfig
    ├── .gitignore
    ├── CHANGELOG.md
    ├── LICENSE
    ├── package.json
    ├── pom.xml
    ├── README.md
    ├── tsconfig.json
    └── yarn.lock

1.  **`node_modules/`**: This directory contains all of the modules of code that your project depends on (npm packages) are automatically installed.

2.  **`src/`**: This directory will contain all of the code related to what you see or not. `src` is a convention for “source code”.
    1. **`main/`**
        1. **`java/`**: This directory contain the Java code.
        2. **`js/`**: This directory contains the TypeScript code for the web ui, inclusive unit tests: suffixed with `.test.ts` or `.test.tsx`
        3. **`resources/`**: This directory contains the the classpath resources.
    2. **`test/`**
        1. **`java/`**: This directory contains the Java unit tests.
        3. **`resources/`**: This directory contains classpath resources for unit tests.
    3. **`target/`**: This is the build directory.
    
3.  **`.editorconfig`**: This is a configuration file for your editor using [EditorConfig](https://editorconfig.org/). The file specifies a style that IDEs use for code.

4.  **`.gitignore`**: This file tells git which files it should not track / not maintain a version history for.

5.  **`CHANGELOG.md`**: All notable changes to this project will be documented in this file.

6.  **`LICENSE`**: This project is licensed under the AGPLv3 license.

7.  **`package.json`**: Here you can find the dependency/build configuration and dependencies for the frontend.

8.  **`pom.xml`**: Maven configuration, which also includes things like metadata.

9.  **`README.md`**: This file, containing useful reference information about the project.

10. **`tsconfig.json`** This is the typescript configuration file.

11. **`yarn.lock`**: This is the ui dependency configuration.

## UI Extensions

Plugins are able to extend or modify the ui of SCM-Manager.
In order to extend the ui the plugin requires a `package.json` in the project root e.g:

```json
{
  "name": "@scm-manager/scm-readme-plugin",
  "main": "src/main/js/index.tsx",
  "scripts": {
    "build" : "ui-scripts plugin",
    "watch" : "ui-scripts plugin-watch",
    "test" : "jest",
    "postinstall" : "ui-plugins postinstall"
  },
  "dependencies": {
    "@scm-manager/ui-plugins" : "2.0.0"
  }
}

```

The `main` field of the `package.json` describes the main entry point of the plugin.
The file specified at `main` should use the `binder` from the [@scm-manager/ui-extensions](../../ui-extensions) in oder to bind its extensions.

If the plugins gets build (`mvn package` or `mvn install`), the [buildfrontend-maven-plugin](https://github.com/sdorra/buildfrontend-maven-plugin), will call the `build` script of `package.json`.
The build script triggers the `plugin` command of `@scm-manager/ui-scripts`.
The `ui-scripts` will do the following steps:

* traverses the import statements of the script specified at `main`
* transpiles TypeScript to es5
* creates a single bundle
* stores the bundle in the final smp package

At runtime the plugins are loaded by PluginLoader. The PluginLoader is a React component, which does the following steps:

* fetches plugin metadata (name and registered bundles) from the rest service
* fetches each bundle of every plugin
* executes each bundle
* starts the rest of the application

## Static web resources

A plugin can also store static files in the `src/main/webapp` directory. 
All files of the webapp directory can be resolved relative to the root of the application e.g. the file 
`src/main/webapp/images/logo.jpg` of a plugin can be resolved at `http://localhost:8081/scm/images/logo.jpg`
assuming SCM-Manager is running at `http://localhost:8081/scm`.

# SCM-Manager v2 Plugin Development

## Build and testing

The plugin can be compiled and packaged with the normal maven lifecycle:

* compile - `mvn compile` - compiles Java code and creates the ui bundle
* test - `mvn test` - executes test for Java and JavaScript
* package - `mvn package` - creates the final plugin bundle (smp package) in the target folder
* install - `mvn install` - installs the plugin (smp and jar) in the local maven repository
* deploy - `mvn deploy` - deploys the plugin (smp and jar) to the configured remote repository
* clean - `mvn clean` - removes the target directory

For the development and testing the `serve` lifecycle of the plugin can be used:

* run - `mvn run` - starts scm-manager with the plugin pre installed.

If the plugin was started with `mvn run`, the default browser of the os should be automatically opened.
If the browser does not start automatically, start it manually and go to [http://localhost:8081/scm](http://localhost:8081/scm).

In this mode each change to web files (src/main/js or src/main/webapp), should trigger a reload of the browser with the made changes.
If you compile a class (e.g.: with your íde from src/main/java to target/classes), 
the SCM-Manager context will restart automatically. So you can see your changes without restarting the server.

## Directory/File structure

### Directories

* src/main/java (contains the Java code)
* src/main/resources (contains the the classpath resources)
* src/main/webapp (contains static files, which are accessible by the web ui)
* src/main/js (contains the JavaScript code for the web ui, inclusive unit tests: suffixed with `.test.js`)
* src/test/java (contains the Java unit tests)
* src/test/resources (containers classpath resources for unit tests)
* target (build directory)

### Files

* pom.xml (Maven configuration)
* package.json (ui dependency/build configuration)
* yarn.lock (ui dependency configuration)
* .eslintrc (ui linter configuration)
* .flowconfig (ui typecheck configuration)
* .babelrc (ui javascript language level configuration)
* src/main/resource/locale/(de|en)/plugins.json (i18n configuration, see [i18n for plugins](i18n%20for%20Plugins.md))
* META-INF/scm/plugin.xml (plugin descriptor)


## UI Extensions

Plugins are able to extend or modify the ui of SCM-Manager.
In order to extend the ui the plugin requires a `package.json` in the project root e.g:

```json
{
  "name": "@scm-manager/scm-readme-plugin",
  "main": "src/main/js/index.js",
  "scripts": {
    "build": "ui-bundler plugin"
  },
  "dependencies": {
    "@scm-manager/ui-extensions": "^0.0.6"
  },
  "devDependencies": {
    "@scm-manager/ui-bundler": "^0.0.3"
  }
}

```

The `main` field of the `package.json` describes the main entry point of the plugin.
The file specified at `main` should use the `binder` from the [@scm-manager/ui-extensions](https://bitbucket.org/scm-manager/ui-extensions) in oder to bind its extensions.
For more information of extensions, binder and extension points, please have a look at the [readme](https://bitbucket.org/scm-manager/ui-extensions/src/master/README.md) of [@scm-manager/ui-extensions](https://bitbucket.org/scm-manager/ui-extensions).

If the plugins gets build (`mvn package` or `mvn install`), the [buildfrontend-maven-plugin](https://github.com/sdorra/buildfrontend-maven-plugin), will call the `build` script of `package.json`.
The build script triggers the `plugin` command of the [@scm-manager/ui-bundler](https://bitbucket.org/scm-manager/ui-bundler).
The `ui-bundler` will do the following steps:

* traverses the import statements of the script specified at `main`
* transpiles flow/es@next to es5
* creates a single bundle
* registers the bundle in the plugin.xml
* stores the bundle in the final scmp package

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

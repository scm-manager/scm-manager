<p align="center">
  <a href="https://scm-manager.org/">
    <img alt="SCM-Manager" src="https://download.scm-manager.org/images/logo/scm-manager_logo.png" width="500" />
  </a>
</p>

The easiest way to share and manage your Git, Mercurial and Subversion
repositories.

- Very easy installation
- No need to hack configuration files, SCM-Manager is completely
  configurable from its Web-Interface
- No Apache and no database installation required
- Central user, group and permission management
- Out of the box support for Git, Mercurial and Subversion
- Full RESTFul Web Service API (JSON and XML)
- Rich User Interface
- Simple Plugin API
- Useful plugins available
- Licensed under the MIT-License

This branch (`develop`) is for the development of SCM-Manager 2.x. If you are interested in the development of version
1.x, please checkout the branch `support/1.x`.

## News

All news regarding SCM-Manager will be published in our [blog](https://scm-manager.org/blog/).

## Support / Community
 [Contact the SCM-Manager community support](https://scm-manager.org/support/)

## Documentation

You can find the complete documentation on our [homepage](https://scm-manager.org/docs/).

## Development

The build of SCM-Manager requires the following installed packages:

* Git
* JDK 11
* Mercurial (required for tests)
* Docker (required for the docker package)

The build of SCM-Manager requires Java 11. 

### Tasks

SCM-Manager uses [Gradle](https://gradle.org/) for the build.
The build itself is organized in tasks, the tasks can be executed with the gradle wrapper:

```bash
# on *nix
./gradlew taskname

# on windows
gradlew.bat taskname
```

This following tables describes some high level tasks,
which should cover most of the daily work.

| Name | Description |
| ---- | ----------- |
| run | Starts an SCM-Manager with enabled livereload for the ui |
| build | Executes all checks, tests and builds the smp inclusive javadoc and source jar |
| distribution | Builds all distribution packages of scm-packaging |
| check | Executes all registered checks and tests |
| test | Run all unit tests |
| integrationTest | Run all integration tests of scm-it |
| clean | Deletes the build directory |

The next table defines a few more tasks which are more relevant for CI servers.

| Name | Description |
| ---- | ----------- |
| publish | Publishes all artifacts and packages (required properties, see section 'Properties for publishing') |
| sonarqube | Executes a SonarQube analysis |
| setVersion | Sets the version to a new version |
| setVersionToNextSnapshot | Sets the version to the next snapshot version |

There many more tasks, which are executed as part of the high level tasks,
and it should rarely be necessary to call them individually.
To see the full list of available tasks, execute the following command:

```bash
# on *nix
./gradlew tasks

# on windows
gradlew.bat tasks
```

### Dependencies

Dependencies and their versions are configured in the `gradle/dependencies.gradle`.
Versions of dependencies can be specified as exact version or as a [range](https://docs.gradle.org/current/userguide/single_versions.html) of versions.
In order to keep the build consistent and reproducible, we use [gradle dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html).
Whenever a dependency was added, changed or removed the lock files must be refreshed e.g.:

```bash
# on *nix
./gradlew resolveAndLockAll --write-locks

# on windows
gradlew.bat resolveAndLockAll --write-locks
```

### Artifacts and reports

Artifacts and reports which are created from the tasks are stored in the build directory of each subproject.

### Debugging

If you want to debug the `run` task of SCM-Manager.
You can provide the `--debug-jvm` option, which starts the SCM-Manager jvm in debug mode.
Then you can attach a debugger on port 5005.
The port can be changed by using the `--debug-port` e.g.: `--debug-port=5006`.
If you want to wait until a debugger is attached, before SCM-Manager starts you can use the `--debug-wait` option.

### Distribution

SCM-Manager provides various modules to deploy SCM-Manager on differnt platforms (e.g. Docker, Helm, RPM, DEB, Windows).
Those modules are not build by default. 
To build the distribution modules specify the `enablePackaging` property e.g.:

```bash
# on *nix
./gradlew -PenablePackaging distribution

# on windows
gradlew.bat -PenablePackaging distribution
```

The command above will refresh the lock files of all sub projects and all configurations.

### Properties for publishing

The publishing process requires the following properties for authentication and signing.
Those properties should be stored in `~/.gradle/gradle.properties`.

| Property | Description |
| -------- | ----------- |
| packagesScmManagerUsername | Username for [packages.scm-manager.org](https://packages.scm-manager.org) |
| packagesScmManagerPassword | Password for [packages.scm-manager.org](https://packages.scm-manager.org) |
| dockerUsername | Username for [Docker Hub](https://hub.docker.com/) |
| dockerPassword | Password or Api Token for [Docker Hub](https://hub.docker.com/) |
| gitHubApiToken | Api Token for [GitHub](https://github.com/) |
| npmEmail | Email of [NPM](https://www.npmjs.com/) account |
| npmToken | Access Token for [NPM](https://www.npmjs.com/) account |
| signing.keyId | Id of gpg secret key for signing |
| signing.password | Passphrase of gpg secret key |
| signing.secretKeyRingFile | Path to gpg secret key ring file |

## Need help?

Looking for more guidance? Full documentation lives on our [homepage](https://scm-manager.org/docs/) or the
dedicated pages for our [plugins](https://scm-manager.org/plugins/). Do you have further ideas or need support?

- **Community Support** - Contact the SCM-Manager support team for questions about SCM-Manager, to report bugs or to
    request features through the official channels. [Find more about this here](https://scm-manager.org/support/).

- **Enterprise Support** - Do you require support with the integration of SCM-Manager into your processes, with the
    customization of the tool or simply a service level agreement (SLA)? **Contact our development partner Cloudogu!
    Their team is looking forward to discussing your individual requirements with you and will be more than happy to
    give you a quote.** [Request Enterprise Support](https://platform.cloudogu.com/en/support/).

---
title: Windows
subtitle: Install scm-manager on windows
displayToc: true
---

The following document describes the installation process for SCM-Manager on Windows.

## Install Java

SCM-Manager requires at least Java 8, but we recommend Java 11 at the moment.
We support Oracle JRE or OpenJDK, choose one of them:

* [Oracle JRE](https://www.oracle.com/java/technologies/javase-downloads.html#JDK11)
* [OpenJDK](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot)

Download Java and follow the installation instructions.

## Install SCM-Manager

To install SCM-Manager you have to download the latest Windows package from the [download page](/download/).
Once you have downloaded the package you have to extract it to your install location e.g.: `C:\Program Files\SCM-Manager`.
Open the `scm-server.xml` and configure the location of your Java installation e.g.:

```xml
<env name="JAVA_HOME" value="C:\Program Files\Java\jre-11.0.7" />
<executable>%JAVA_HOME%\bin\java</executable>
```

Now we have to open a Terminal (PowerShell, Bash or CMD), in order to install the SCM-Manager as Windows Service.
Navigate to the location of the SCM-Manager installation with the terminal and execute the following commands to install and start the service.

```bash
scm-server.exe install
scm-server.exe start
```

SCM-Manager is now starting and after a few seconds it should be reachable on port 8080.
There you have to create your initial admin account with an initialization token which you can find inside your server logs.
You can find more detailed information here: [first startup](../../first-startup/)

## Troubleshooting

If the process comes not up or the port is not open have a look at the logs.
The logs are located in the SCM-Manager install directory in a folder named `logs`.

## Home directory

SCM-Manager stores all its information in its home directory.
The home directory is located at `%APPDATA%\SCM-Manager`.
`%APPDATA%` should be the `AppData\Roaming` directory of the account which starts the service.
In the default configuration the [LocalSystem](https://docs.microsoft.com/windows/win32/services/localsystem-account) account is used,
that means that your data is stored at `C:\Windows\System32\config\systemprofile\AppData\Roaming\SCM-Manager`.

If you want to change the location of the home directory add and environment variable to the scm-server.xml e.g.:

```xml
<env name="SCM_HOME" value="D:\SCM-Manager" />
```

## Configuration

Most of the configuration of scm-manager can be configured via the web interface.
But the startup and the web server configuration must be configured via configuration files.
The default configuration of the windows package should match 90% of the use cases,
if you have to change something ensure you know what you are doing.

The startup can be configured via the `scm-server.xml`, have a look at the [configuration options](https://github.com/winsw/winsw/blob/master/doc/xmlConfigFile.md).
To configure logging and the webserver, `conf` in the installation directory is the right place.

---
title: Base Directory
---

The SCM-Manager base directory aka. home directory, 
contains all data which is created by SCM-Manager such as repositories and configurations.
The location of the base directory depends on your operating system and type of installation.

| Type of Installation | Base directory |
|----------------------|----------------|
| Docker | /var/lib/scm |
| RPM | /var/lib/scm |
| DEB | /var/lib/scm |
| Unix | ~/.scm |
| Mac OS X | ~/Library/Application Support/SCM-Manager |
| Windows | %APPDATA%\SCM-Manager |

## Change base directory location

The location of the base directory can be changed by using one of the following ways.
The preferences are the following: Properties file over system property over environment variable.

### Environment variable

By setting the environment variable **SCM_HOME** e.g.:

```bash
export SCM_HOME=/home/scm
/opt/scm-server/bin/scm-server
```

For rpm and deb installations the variable can be changed via the file `/etc/default/scm-server`.   

## System property

The path can be changed by setting the system property **scm.home** e.g.:

```bash
-Dscm.home=/home/scm
```
## Properties file

If SCM-Manager finds a file called `scm.properties` on the class path it reads the property `scm.home` e.g.:

```properties
scm.home=/home/scm
```

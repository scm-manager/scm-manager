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

The location of the base directory can be changed by setting the environment variable **SCM_WEBAPP_HOMEDIR**.

```bash
export SCM_WEBAPP_HOMEDIR=/home/scm
/opt/scm-server/bin/scm-server
```

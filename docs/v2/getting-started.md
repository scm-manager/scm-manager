# Getting started

### Install Java

SCM-Manager needs an installed Java 1.8 or newer. It is recommended to use the [oracle jre](http://java.oracle.com/). 
How to check which version of Java is installed:

```bash
java -version
```

Download java from [here](http://java.oracle.com/) and follow the install instructions.

### Install SCM-Manager

#### Standalone Server

Download the latest version of SCM-Manager from
[Nexus](https://maven.scm-manager.org/nexus/#nexus-search;gav~sonia.scm~scm-server~2.*~~), 
extract the downloaded .zip or .tar.gz archive and start SCM-Manager 2 with

```bash
scm-server/bin/scm-server
```

or 

```bash
scm-server\bin\scm-server.bat
```

#### Docker

To start SCM-Manager with a persistent volume on port 8080 run the following command:

```bash
docker run -p 8080:8080 -v scm-home:/var/lib/scm --name scm scmmanager/scm-manager:2.0.0-latest
```

### First access

Your SCM-Manager should be running on port 8080. You can access it locally via <http://localhost:8080>.

|              |                         |
| ------------ | ----------------------- |
| **Username** | scmadmin                |
| **Password** | scmadmin                |

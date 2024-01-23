---
title: Docker
subtitle: Install scm-manager with docker
displayToc: true
---

## Quickstart

Grab the latest version from [download page](/download) and replace `<version>` in the code blocks below

```bash
docker run --name scm -p 8080:8080 -v scm-home:/var/lib/scm scmmanager/scm-manager:<version>
```

for example:

```bash
docker run --name scm -p 8080:8080 -v scm-home:/var/lib/scm scmmanager/scm-manager:2.0.0
```

## Configuration

You can configure your server by using environment variables or adjusting the `config.yml`.

### config.yml

The `config.yml` can be copied from your container, e.g.

```shell
docker cp scm:/etc/scm/config.yml ${PWD}/config.yml  
```

After you have customized the configuration to your needs, you can restart the container and mount this `config.yml` to
override the default configuration, e.g.

```shell
docker run -v ${PWD}/config.yml:/etc/scm/config.yml scmmanager/scm-manager
```

### Environment variables

Each field of the server configuration can be overridden by the related environment variable.
The variables follow the same structure as the fields in the server config, e.g.

```shell
docker run -e "SCM_CONTEXT_PATH=/scm42" scmmanager/scm-manager
```

To find more examples, please check the [server configuration docs](../administration/scm-server.md).

## Persistence

It is recommended to create a persistent volume for the scm-manager home directory.
This allows scm-manager updates and recreation of the container without lose of data.
The home directory is located at `/var/lib/scm`.
It is recommended to use a volume managed by docker.
If it is required to use a host directory, keep in mind that the scm-manager process is executed with a user which has
the id 1000.
So ensure that the user with the uid 1000 can write to the directory e.g.:

```text
mkdir /scm_home
chown 1000:1000 /scm_home
docker run --name scm -p 8080:8080 -v /scm_home:/var/lib/scm scmmanager/scm-manager:<version>
```

## Exposed Ports

SCM-Manager exposes its http port on port 8080.
If you want to use the ssh plugin, keep in mind that this plugin requires an extra port (default is 2222).

```text
docker run --name scm -p 2222:2222 -p 8080:8080 -v scm-home:/var/lib/scm scmmanager/scm-manager:<version>
```

## JVM Parameters

If it becomes necessary to add JVM parameters to the start, there are two ways to do this:

* As arguments e.g.:

```bash
docker run scmmanager/scm-manager:<version> -Dsome.property=value
```

* Or as JAVA_OPTS environment variable e.g.:

```bash
docker run -e JAVA_OPTS="-Dsome.property=value" scmmanager/scm-manager:<version>
```

## Docker Compose

If you want to use the image with docker-compose have a look at the example below.

```yaml
version: '2.0'
services:
  scm:
    image: scmmanager/scm-manager:<version>
    ports:
      - "8080:8080"
      # if the ssh plugin is used
      - "2222:2222"
    volumes:
      - scmhome:/var/lib/scm
volumes:
  scmhome: { }
```

## Variants

We are offer two variants of the SCM-Manager docker image one with OpenJDK on Alpine and the other with Eclipse Temurin
on Debian.

### scmmanager/scm-manager:<version>-alpine

This image uses the Alpine operating system and the OpenJDK distribution from the official apk repository.
The image is available for the following os/architectures:

* linux/amd64
* linux/arm64

### scmmanager/scm-manager:<version>-debian

This image uses the Debian operating system and the Eclipse Temurin JDK.
The image is available for the following os/architectures:

* linux/amd64
* linux/arm64
* linux/arm/v7

### scmmanager/scm-manager:<version>

The default image is mainly an alias for the alpine variant, but there is no alpine variant for arm/v7.
For arm/v7 the debian variant is used.

* linux/amd64 uses the alpine variant
* linux/arm64 uses the alpine variant
* linux/arm/v7 uses the debian variant

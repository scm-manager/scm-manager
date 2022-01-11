---
title: Mac OS X
subtitle: SCM-Manager installation on OS X using homebrew
displayToc: true
---

To install SCM-Manager on OS X we offer a [Homebrew](https://brew.sh/) tap.

## Quickstart

```bash
brew install scm-manager/tap/scm-server
brew services start scm-manager/tap/scm-server
```

After a few seconds SCM-Manager should be started on port 8080.
There you have to create your initial admin account with an initialization token which you can find inside your server logs.
You can find more detailed information here: [first startup](../../first-startup/)

## Detailed installation

To install SCM-Manager with homebrew we had to add the SCM-Manager tap:

```bash
brew tap scm-manager/tap
```

After the tap was added, we can install SCM-Manager:

```bash
brew install scm-server
```

Now SCM-Manager can be started:

```bash
scm-server
```

If you want to start it in the background as OSX service:

```bash
brew services start scm-manager/tap/scm-server
```

After a few seconds SCM-Manager should be started on port 8080.
The default username is `scmadmin` with the password `scmadmin`.

## Troubleshooting

If the service does not start have a look at the logs `~/Library/Logs`.

## Home directory

SCM-Manager stores all its information in its home directory at `~/Library/Application\ Support/SCM-Manager`.

## Configuration

Most of the configuration of scm-manager can be configured via the web interface.
But the startup and the web server configuration must be configured via configuration files.
The default configuration of the debian package should match 90% of the use cases,
if you have to change something ensure you know what you are doing.

To configure the startup have a look at `/usr/local/opt/scm-server/libexec/bin/scm-server`.
To configure logging and the webserver, `/usr/local/opt/scm-server/libexec/conf` is the right place.

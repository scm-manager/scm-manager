---
title: SCM-Server Configuration
subtitle: Various configuration options for the SCM-Server
displayToc: true
---

SCM-Manager v3 can be configured in several ways. We recommend using `config.yml` to have most of the settings in
one place. However, if required, each option in this configuration can also be set via environment variables.
See the relevant topics below for more information.

## Webserver Configuration

The listener host and port of your SCM-Server can directly be edited in the top level of your `config.yml`.
If you want your server without a context path (use `root path`), you can change this option to be `/`.

**config.yml**

```yaml
# This is the host adresse, `0.0.0.0` means it listens on every interface
addressBinding: 0.0.0.0
# This is the exposed port for your application
port: 8080
contextPath: /
httpHeaderSize: 16384
```

**Environment variables**

| Environment Variable | Corresponding config.yml property | Example                            |
| -------------------- | --------------------------------- | ---------------------------------- |
| SCM_ADDRESS_BINDING  | addressBinding                    | export SCM_ADDRESS_BINDING=0.0.0.0 |
| SCM_PORT             | port                              | export SCM_PORT=8080               |
| SCM_CONTEXT_PATH     | contextPath                       | export SCM_CONTEXT_PATH=/          |
| SCM_HTTP_HEADER_SIZE | httpHeaderSize                    | export SCM_HTTP_HEADER_SIZE=16384  |

## SSL

In order to use https with SCM-Server, you need a keystore with a certificate and the corresponding secret key.
In the following we will use openssl to create a self-signed certificate for demonstration purposes.

**Warning**: Do not use self-signed certificates in production, this is only for demonstration purposes.

```bash
openssl req -new -x509 -newkey rsa:2048 -sha256 -keyout tls.key -out tls.crt
```

This command will ask a few questions about metadata for generated certificate:

- PEM pass phrase: This is a password to protect the scret key
- Country Name (2 letter code)
- State or Province Name (full name)
- Locality Name (eg, city)
- Organization Name (eg, company)
- Organizational Unit Name (eg, section)
- Common Name (eg, fully qualified host name)
- Email Address

Make sure that the common name matches the fqdn, which you are using to access SCM-Manager.

### Browsers

In order to use a self-signed certificate the certificate must be imported into you browser.

### Configure Git

To use git with a self-signed certificate, we have to add the certificate path to the configuration.

```bash
git config http.sslCAInfo /complete/path/to/tls.crt
```

### Configure Mercurial

To use mercurial with a self-signed certificate, we have to add the certificate path to the configuration.

```ini
[web]
cacerts = /complete/path/to/cert.pem
```

### Create keystore

Create a keystore in pkcs12 format. This command can be used with the self-signed certificate from above or with a valid
certificate from an authority.

```bash
openssl pkcs12 -inkey tls.key -in tls.crt -export -out keystore.pkcs12
```

If your secret key is protected with a passphrase, you must enter this first. You must then enter an export password to
protect your keystore.

### Server configuration

Adjust your `config.yml` to apply your prepared keystore with configured certificate or set them via environment variables.

**config.yml**

```yaml
https:
  # If the key store path is not set, the https config will be ignored entirely.
  # This must be set to your created keystore from above.
  keyStorePath: /conf/keystore.pkcs12
  # The password of your keystore.
  keyStorePassword: secret
  # The type of your keystore. Use pkcs12 or jks for java keystore.
  keyStoreType: PKCS12
  # The port of your https connector
  sslPort: 443
  # Automatically redirects incoming http requests to this https connector
  redirectHttpToHttps: true
```

**Environment variables**

| Environment Variable             | Corresponding config.yml property | Example                                               |
| -------------------------------- | --------------------------------- | ----------------------------------------------------- |
| SCM_HTTPS_KEY_STORE_PATH         | https.keyStorePath                | export SCM_HTTPS_KEY_STORE_PATH=/conf/keystore.pkcs12 |
| SCM_HTTPS_KEY_STORE_PASSWORD     | https.keyStorePassword            | export SCM_HTTPS_KEY_STORE_PASSWORD=secret            |
| SCM_HTTPS_KEY_STORE_TYPE         | https.keyStoreType                | export SCM_HTTPS_KEY_STORE_TYPE=PKCS12                |
| SCM_HTTPS_SSL_PORT               | https.sslPort                     | export SCM_HTTPS_PORT=443                             |
| SCM_HTTPS_REDIRECT_HTTP_TO_HTTPS | https.redirectHttpToHttps         | export SCM_HTTPS_REDIRECT_HTTP_TO_HTTPS=true          |

## Change directories

The default directories are platform-specific and therefore could be different if you try scm-server on different
operational systems. Paths starting with `/` are absolute to your file system. If you use relative paths without a
starting `/`, your configured path will be located relative to your base directory of your scm-server (
like `/opt/scm-server`
on unix-based packages).

For technical reasons the tempDir is located at the top level of your `config.yml`. All other path-based config options
are located under `webapp`.

**config.yml**

```yaml
tempDir: /tmp

webapp:
  homeDir: ./scm-home
  workDir: /etc/scm/work
```

**Environment variables**

| Environment Variable | Corresponding config.yml property | Example                                 |
| -------------------- | --------------------------------- | --------------------------------------- |
| SCM_TEMP_DIR         | tempDir                           | export SCM_TEMP_DIR=/tmp                |
| SCM_WEBAPP_HOMEDIR   | webapp.homeDir                    | export SCM_WEBAPP_HOMEDIR=./scm-home    |
| SCM_WEBAPP_WORKDIR   | webapp.workDir                    | export SCM_WEBAPP_WORKDIR=/etc/scm/work |

## Reverse proxy

If your SCM-Manager instance is behind a reverse proxy like NGINX, you most likely will have to enable
X-Forward-Headers.
These HTTP headers are being appended to the requests which are redirected by your reverse proxy server. Without
this option set, your SCM-Server may run into connection issues. This option is disabled by default, because without a
reverse proxy it could cause security issues.

Many reverse proxies will also cache response streams. This can lead to timeouts, especially when working with large
repositories. To avoid this, you might want to increase the `idleTimeout` to a higher value, depending on the size of
your repositories (you might want to start with `300000`, that would be five minutes).

**config.yml**

```yaml
forwardHeadersEnabled: true
idleTimeout: 300000
```

**Environment variables**

| Environment Variable        | Corresponding config.yml property | Example                                 |
| --------------------------- | --------------------------------- | --------------------------------------- |
| SCM_FORWARD_HEADERS_ENABLED | forwardHeadersEnabled             | export SCM_FORWARD_HEADERS_ENABLED=true |
| SCM_IDLE_TIMEOUT            | idleTimeout                       | export SCM_IDLE_TIMEOUT=300000          |

## Webapp

The webapp configuration consists of anything that is not webserver or logging related.
Most of the available options should be set to the recommended values of your default `config.yml` file.
You can also override these options with environment variables.

**config.yml**

```yaml
webapp:
  ## Sets explicit working directory for internal processes, empty means default java temp dir
  workDir:
  ## Home directory "scm-home" which is also set for classpath
  homeDir: /var/lib/scm
  cache:
    dataFile:
      enabled: true
    store:
      enabled: true
  # name of initial admin user (this is normally set over the ui on the first start)
  initialUser: scmadmin
  # password of initial admin user (this is normally set over the ui on the first start)
  initialPassword: scmadmin
  # if true skip the creation of initial admin user completely
  skipAdminCreation: false
  ## Warning: Enabling this option can lead to security issue.
  endlessJwt: false
  ## Number of async threads
  asyncThreads: 4
  ## Max seconds to abort async execution
  maxAsyncAbortSeconds: 60
  ## Amount of central work queue workers
  centralWorkQueue:
    workers: 4
  ## Strategy for the working copy pool implementation [sonia.scm.repository.work.NoneCachingWorkingCopyPool, sonia.scm.repository.work.SimpleCachingWorkingCopyPool]
  workingCopyPoolStrategy: sonia.scm.repository.work.SimpleCachingWorkingCopyPool
  ## Amount of "cached" working copies
  workingCopyPoolSize: 5
```

**Environment variables**

| Environment Variable                | Corresponding config.yml property | Example                                                                                          |
| ----------------------------------- | --------------------------------- | ------------------------------------------------------------------------------------------------ |
| SCM_WEBAPP_WORKDIR                  | webapp.workDir                    | export SCM_WEBAPP_WORKDIR=/tmp/scm-work                                                          |
| SCM_WEBAPP_HOMEDIR                  | webapp.homeDir                    | export SCM_WEBAPP_HOMEDIR=/var/lib/scm                                                           |
| SCM_WEBAPP_CACHE_DATAFILE_ENABLED   | webapp.cache.datafile.enabled     | export SCM_WEBAPP_CACHE_DATAFILE_ENABLED=true                                                    |
| SCM_WEBAPP_CACHE_STORE_ENABLED      | webapp.cache.store.enabled        | export SCM_WEBAPP_CACHE_STORE_ENABLED=true                                                       |
| SCM_WEBAPP_ENDLESSJWT               | webapp.endlessJwt                 | export SCM_WEBAPP_ENDLESSJWT=false                                                               |
| SCM_WEBAPP_ASYNCTHREADS             | webapp.asyncThreads               | export SCM_WEBAPP_ASYNCTHREADS=4                                                                 |
| SCM_WEBAPP_MAXASYNCABORTSECONDS     | webapp.maxAsyncAbortSeconds       | export SCM_WEBAPP_MAXASYNCABORTSECONDS=60                                                        |
| SCM_WEBAPP_CENTRALWORKQUEUE_WORKERS | webapp.centralWorkQueue.workers   | export SCM_WEBAPP_CENTRALWORKQUEUE_WORKERS=4                                                     |
| SCM_WEBAPP_WORKINGCOPYPOOLSTRATEGY  | webapp.workingCopyPoolStrategy    | export SCM_WEBAPP_WORKINGCOPYPOOLSTRATEGY=sonia.scm.repository.work.SimpleCachingWorkingCopyPool |
| SCM_WEBAPP_WORKINGCOPYPOOLSIZE      | webapp.workingCopyPoolSize        | export SCM_WEBAPP_WORKINGCOPYPOOLSIZE=5                                                          |
| SCM_WEBAPP_INITIALUSER              | webapp.initialUser                | export SCM_WEBAPP_INITIALUSER=scmadmin                                                           |
| SCM_WEBAPP_INITIALPASSWORD          | webapp.initialPassword            | export SCM_WEBAPP_INITIALPASSWORD=scmadmin                                                       |
| SCM_WEBAPP_SKIPADMINCREATION        | webapp.skipAdminCreation          | export SCM_WEBAPP_SKIPADMINCREATION=true                                                         |

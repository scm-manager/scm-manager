---
title: SCM-Server Configuration
subtitle: Various configuration options for the SCM-Server
displayToc: true
---

SCM-Manager v3 can be configured in several ways. We recommend using `config.yml` to have most of the settings in
one place. However, if required, each option in this configuration can also be set via environment variables.
See the relevant topics below for more information.

## Logging

The log level can be configured in the `config.yml`.
You can change the root log level to change the log level globally for all loggers.
Also, new specific logger can be added to control logging in a fine-grained style.
We provide two types of log appender.
`fileAppender` which writes the logging output to a defined log file and the `consoleAppender` which simply prints
everything to your (bash/shell) console.
Depending on which platform your scm-server is running, we already configured the recommended logging settings.

**Example**

```yaml
log:
  # General logging level
  rootLevel: WARN
  enableFileAppender: true
  enableConsoleAppender: true

  # Custom specific loggers
  # The "name" has to be the path of the classes to be logged with this logger
  logger:
    - name: sonia.scm
      level: DEBUG
    - name: com.cloudogu.scm
      level: DEBUG
```

To override this config with environment variables you could set it like:

`SCM_LOG_ROOT_LEVEL` to one of the log levels, like `DEBUG`
`SCM_LOG_FILE_APPENDER_ENABLED` to one of the log levels, like `true`
`SCM_LOG_CONSOLE_APPENDER_ENABLED` to one of the log levels, like `false`
`SCM_LOG_LOGGER` with a comma-separated list of your loggers, like `sonia.scm:DEBUG,com.cloudogu.scm:TRACE`

Supported log levels are: TRACE, DEBUG, INFO, WARN, ERROR

### Logback

If you want to configure more advanced loggers which are beyond this simple configuration, you may still use
a logback configuration file.
You have to enable your logback configuration by setting the file path with the system
property `logback.configurationFile`, like `-Dlogback.configurationFile=logging.xml`.
If the logback configuration is enabled, the log configuration of the `config.yml` will be ignored.

**Example**

```xml

<configuration>
  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>/var/log/scm/scm-manager.log</file>

    <rollingPolicy class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy">
      <fileNamePattern>/var/log/scm/scm-manager-%i.log</fileNamePattern>
      <minIndex>1</minIndex>
      <maxIndex>10</maxIndex>
    </rollingPolicy>

    <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
      <maxFileSize>10MB</maxFileSize>
    </triggeringPolicy>

    <append>true</append>
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%-10X{transaction_id}] %-5level %logger - %msg%n</pattern>
    </encoder>
  </appender>

  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">

    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%-10X{transaction_id}] %-5level %logger - %msg%n</pattern>
    </encoder>

  </appender>

  <logger name="sonia.scm" level="INFO"/>
  <logger name="com.cloudogu.scm" level="INFO"/>

  <!-- suppress massive gzip logging -->
  <logger name="sonia.scm.filter.GZipFilter" level="WARN"/>
  <logger name="sonia.scm.filter.GZipResponseStream" level="WARN"/>

  <logger name="sonia.scm.util.ServiceUtil" level="WARN"/>

  <!-- event bus -->
  <logger name="sonia.scm.event.LegmanScmEventBus" level="INFO"/>


  <root level="WARN">
    <appender-ref ref="FILE"/>
  </root>

</configuration>
```

## Webserver Configuration

The listener host and port of your SCM-Server can directly be edited in the top level of your `config.yml`.
If you want your server without a context path (use `root path`), you can change this option to be `/`.

**Example**

```yaml
# This is the host adresse, `0.0.0.0` means it listens on every interface
addressBinding: 0.0.0.0
# This is the exposed port for your application 
port: 8080
contextPath: /
```

To override this config with environment variables you could set it like:

`SCM_SERVER_PORT` to your port, like `8081`
`SCM_SERVER_ADDRESS_BINDING` to the destination ip / hostname, like `0.0.0.0`
`SCM_SERVER_CONTEXT_PATH` to `/myContextPath`

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

Adjust your `config.yml` to apply your prepared keystore with configured certificate.

**Example**

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

## Change directories

The default directories are platform-specific and therefore could be different if you try scm-server on different
operational systems. Paths starting with `/` are absolute to your file system. If you use relative paths without a
starting `/`, your configured path will be located relative to your base directory of your scm-server (
like `/opt/scm-server`
on unix-based packages).

For technical reasons the tempDir is located at the top level of your config.yml. All other path-based config options
are located under `webapp`.

**Example**

```yaml
tempDir: /tmp

webapp:
  homeDir: ./scm-home
  workDir: 
```

To override this config with environment variables you could set it like:

`SCM_TEMP_DIR` to your port, like `/tmp`
`SCM_WEBAPP_HOMEDIR` to home dir (aka `scm-home`), like `./myHomeDir`
`SCM_WEBAPP_WORKDIR` to `/scm-work`

## Reverse proxy

If your SCM-Manager instance is behind a reverse proxy like NGINX, you most likely will have to enable
X-Forward-Headers.
These HTTP headers are being appended to the requests which are redirected by your reverse proxy server. Without
this option set, your SCM-Server may run into connection issues. This option is disabled by default, because without a
reverse proxy it could cause security issues.

**Example**

```yaml
forwardHeadersEnabled: true
```

## Webapp

The webapp configuration consists of anything that is not webserver or logging related.
Most of the available options should be set to the recommended values of your default `config.yml` file.

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

To override this config with environment variables can set the options following the schema `SCM_WEBAPP_PROPERTYNAME`.
All hierarchy levels have to be separated by underscores `_`.

Like:

- `SCM_WEBAPP_CACHE_DATAFILE_ENABLED` = `true`
- `SCM_WEBAPP_MAXASYNCABORTSECONDS` = `45`
- `SCM_WEBAPP_CENTRALWORKQUEUE_WORKERS` = `42`

---
title: Logging
subtitle: Configuration and locations of SCM-Manager logging
---

SCM-Manager logs information which can be useful, if the system does not behave as expected.
The logging behavior depends on your operating system and installation.

| Type of Installation | Logging                    |
|----------------------|----------------------------|
| Docker               | stdout                     |
| RPM                  | /var/log/scm               |
| DEB                  | /var/log/scm               |
| Unix                 | $BASEDIR/logs              |
| Mac OS X             | ~/Library/Logs/SCM-Manager |
| Windows              | $BASEDIR\logs              |

The location of the **$BASEDIR** can be found [here](../basedirectory/).

## Configuration

The logging behaviour of SCM-Manager can be configured via an `config.yml` file or environment variables.
Environment variables override the behaviour defined in the `config.yml`.
The location of the `config.yml` file depends also on the type of installation.

| Type of Installation | Path                                     |
|----------------------|------------------------------------------|
| Docker               | /etc/scm/config.yml                      |
| RPM                  | /etc/scm/config.yml                      |
| DEB                  | /etc/scm/config.yml                      |
| Unix                 | $EXTRACT_PATH/scm-server/conf/config.yml |
| Mac OS X             | $EXTRACT_PATH/scm-server/conf/config.yml |
| Windows              | $EXTRACT_PATH/scm-server/conf/config.yml |

**$EXTRACT_PATH** is the path were you extract the content of the package.

## Configuration via config.yml file

The logging configuration can be set via `config.yml` file.
The configuration could look like this:

```yaml
log:
  # The directory where the log files should be stored
  # This is an optional setting, if it is not set, the default directory will be used
  logDir: /etc/scm/logs
  # The log level that should be used globally
  # Supported log levels are: TRACE, DEBUG, INFO, WARN and ERROR
  rootLevel: WARN
  # Whether the log file should be appended with new logs
  enableFileAppender: true
  # Whether the standard output should be appended with new logs
  enableConsoleAppender: true

  # Specifies which log level should be used for a specific class or package
  # The "name" needs to be a fully qualified classpath or package path
  # This setting overrides the root log level, for the specified class or package
  logger:
    sonia.scm: DEBUG
    com.cloudogu.scm: DEBUG
```

## Configuration via environment variable

Every property that can be set inside the `config.yml` can also be set as an environment variable.
The following table explains which environment variables are available and how to set them.

| Environment Variable             | Corresponding config.yml property | Example                                                      |
|----------------------------------|-----------------------------------|--------------------------------------------------------------|
| SCM_LOG_DIR                      | log.logDir                        | export SCM_LOG_DIR=/etc/scm/logs                             |
| SCM_LOG_ROOT_LEVEL               | log.rootLevel                     | export SCM_LOG_ROOT_LEVEL=WARN                               |
| SCM_LOG_FILE_APPENDER_ENABLED    | log.enableFileAppender            | export SCM_LOG_FILE_APPENDER_ENABLED=true                    |
| SCM_LOG_CONSOLE_APPENDER_ENABLED | log.enableConsoleAppender         | export SCM_LOG_CONSOLE_APPENDER_ENABLED=true                 |
| SCM_LOG_LOGGER                   | log.logger                        | export SCM_LOG_LOGGER=sonia.scm:DEBUG,com.cloudogu.scm:DEBUG |

## Logback

If the configuration properties are not sufficient, then you may configure Logback itself.
Logback is the library that the SCM-Manager uses for its logging.
To enable a custom Logback configuration, you have to start the SCM-Manager with the following system property `-Dlogback.configurationFile=logging.xml`.
If this property is set, then the configuration inside the `config.yml` and environment variables will be ignored.
Then depending on the type of installation you use the `logging.xml` file is expected at the following directories:

| Type of Installation | Path                                      |
|----------------------|-------------------------------------------|
| Docker               | /etc/scm/logging.xml                      |
| RPM                  | /etc/scm/logging.xml                      |
| DEB                  | /etc/scm/logging.xml                      |
| Unix                 | $EXTRACT_PATH/scm-server/conf/logging.xml |
| Mac OS X             | $EXTRACT_PATH/scm-server/conf/logging.xml |
| Windows              | $EXTRACT_PATH/scm-server/conf/logging.xml |

**$EXTRACT_PATH** is the path were you extract the content of the package.

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

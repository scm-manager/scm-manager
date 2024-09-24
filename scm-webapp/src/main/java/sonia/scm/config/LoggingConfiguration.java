/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@SuppressWarnings("java:S3740") // Accept usage of not-parameterized classes
public class LoggingConfiguration {

  private final ServerConfigYaml.LogConfig config;
  private final LoggerContext loggerContext;

  public LoggingConfiguration() {
    this.config = new ServerConfigParser().parse().getLog();
    this.loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
  }

  public void configureLogging() {
    if (!Strings.isNullOrEmpty(System.getProperty("logback.configurationFile"))) {
      System.out.println("Found logback configuration file. Ignoring logging configuration from config.yml.");
      return;
    }

    ch.qos.logback.classic.Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    loggerContext.reset();
    root.setLevel(Level.valueOf(config.getRootLevel()));
    configureSpecificLoggers();

    if (config.isFileAppenderEnabled()) {
      RollingFileAppender fileAppender = configureFileLogger();
      root.addAppender(fileAppender);
    }

    if (config.isConsoleAppenderEnabled()) {
      ConsoleAppender consoleAppender = configureConsoleLogger();
      root.addAppender(consoleAppender);
    }
  }

  private void configureSpecificLoggers() {
    config.getLogger().forEach(this::addLogger);
  }

  private ConsoleAppender configureConsoleLogger() {
    PatternLayoutEncoder consoleLogEncoder = new PatternLayoutEncoder();
    consoleLogEncoder.setContext(loggerContext);
    consoleLogEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%-10X{transaction_id}] %-5level %logger - %msg%n");
    consoleLogEncoder.start();

    ConsoleAppender logConsoleAppender = new ConsoleAppender<>();
    logConsoleAppender.setContext(loggerContext);
    logConsoleAppender.setName("STDOUT");
    logConsoleAppender.setEncoder(consoleLogEncoder);
    logConsoleAppender.start();

    return logConsoleAppender;
  }

  private RollingFileAppender configureFileLogger() {
    String logDirectory = config.getLogDir();

    if (Strings.isNullOrEmpty(logDirectory)) {
      logDirectory = new ScmLogFilePropertyDefiner().getPropertyValue();
    }

    if (System.getProperty("basedir") != null) {
      logDirectory = Path.of(System.getProperty("basedir")).resolve(logDirectory).normalize().toString();
    }
    System.out.println("Writing logs to: " + logDirectory);

    RollingFileAppender logFileAppender = new RollingFileAppender();
    logFileAppender.setFile(logDirectory + "/scm-manager.log");
    logFileAppender.setContext(loggerContext);
    logFileAppender.setName("FILE");

    // Create File Logger
    PatternLayoutEncoder fileLogEncoder = new PatternLayoutEncoder();
    fileLogEncoder.setContext(loggerContext);
    fileLogEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%-10X{transaction_id}] %-5level %logger - %msg%n");
    fileLogEncoder.start();
    logFileAppender.setEncoder(fileLogEncoder);
    logFileAppender.setAppend(true);

    FixedWindowRollingPolicy logFilePolicy = new FixedWindowRollingPolicy();
    logFilePolicy.setContext(loggerContext);
    logFilePolicy.setParent(logFileAppender);
    logFilePolicy.setFileNamePattern(logDirectory + "/scm-manager-%i.log");
    logFilePolicy.setMinIndex(1);
    logFilePolicy.setMaxIndex(10);
    logFilePolicy.start();

    SizeBasedTriggeringPolicy triggeringPolicy = new SizeBasedTriggeringPolicy();
    triggeringPolicy.setMaxFileSize(FileSize.valueOf("10MB"));
    triggeringPolicy.start();

    logFileAppender.setTriggeringPolicy(triggeringPolicy);
    logFileAppender.setRollingPolicy(logFilePolicy);
    logFileAppender.start();

    return logFileAppender;
  }

  private void addLogger(String name, String level) {
    loggerContext.getLogger(name).setLevel(Level.valueOf((level)));
  }
}

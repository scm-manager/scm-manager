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

import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ServerConfigYaml {

  private static final String SCM_PREFIX = "SCM_";

  private LogConfig log = new LogConfig();

  private Map<String, Object> webapp = new HashMap<>();

  public static class LogConfig {
    private String logDir = "";
    private String rootLevel = "INFO";
    private Map<String, String> logger = new HashMap<>();
    @Setter
    private boolean enableFileAppender = true;
    @Setter
    private boolean enableConsoleAppender = true;

    private LogConfig() {}

    public String getLogDir() {
      return getEnvWithDefault("LOG_DIR", logDir);
    }

    public void setLogDir(String logDir) {
      this.logDir = logDir;
    }

    public String getRootLevel() {
      return getEnvWithDefault("LOG_ROOT_LEVEL", rootLevel);
    }

    public Map<String, String> getLogger() {
      String envLoggers = System.getenv("SCM_LOG_LOGGER");
      if (envLoggers != null) {
        String[] envLoggerEntries = envLoggers.split(",");
        Map<String, String> loggerMap = new HashMap<>();
        for (String envLoggerEntry : envLoggerEntries) {
          if (!envLoggerEntry.contains(":")) {
            // Skip because invalid format
            continue;
          }
          String[] envLoggerEntryParts = envLoggerEntry.trim().split(":");
          loggerMap.put(envLoggerEntryParts[0].trim(), envLoggerEntryParts[1].trim());
        }
        return loggerMap;
      }
      return logger;
    }

    public void setRootLevel(String rootLevel) {
      this.rootLevel = rootLevel;
    }

    public void setLogger(Map<String, String> logger) {
      this.logger = logger;
    }

    public boolean isFileAppenderEnabled() {
      return getEnvWithDefault("LOG_FILE_APPENDER_ENABLED", enableFileAppender);
    }

    /**
     * @deprecated As of 3.4.0, because the member variables names have been changed. Therefore, the new setter methods should be used.
     * @param fileAppenderEnabled Whether the file appender should be enabled
     */
    @Deprecated(since = "3.4.0")
    public void setFileAppenderEnabled(boolean fileAppenderEnabled) {
      this.enableFileAppender = fileAppenderEnabled;
    }

    public boolean isConsoleAppenderEnabled() {
      return  getEnvWithDefault("LOG_CONSOLE_APPENDER_ENABLED", enableConsoleAppender);
    }

    /**
     * @deprecated As of 3.4.0, because the member variables names have been changed. Therefore, the new setter methods should be used.
     * @param consoleAppenderEnabled Whether the console appender should be enabled
     */
    @Deprecated(since = "3.4.0")
    public void setConsoleAppenderEnabled(boolean consoleAppenderEnabled) {
      this.enableConsoleAppender = consoleAppenderEnabled;
    }
  }

  public LogConfig getLog() {
    return log;
  }

  public void setLog(LogConfig log) {
    this.log = log;
  }


  public Map<String, Object> getWebapp() {
    return webapp;
  }

  public void setWebapp(Map<String, Object> webapp) {
    this.webapp = webapp;
  }

  static String getEnvWithDefault(String envKey, String configValue) {
    String value = getEnv(envKey);
    return value != null ? value : configValue;
  }

  static boolean getEnvWithDefault(String envKey, boolean configValue) {
    String value = getEnv(envKey);
    return value != null ? Boolean.parseBoolean(value) : configValue;
  }

  private static String getEnv(String envKey) {
    return System.getenv(SCM_PREFIX + envKey);
  }
}

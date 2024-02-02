/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.config;

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
    private boolean fileAppenderEnabled = true;
    private boolean consoleAppenderEnabled = true;

    private LogConfig() {}

    public String getLogDir() {
      return logDir;
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
      return getEnvWithDefault("LOG_FILE_APPENDER_ENABLED", fileAppenderEnabled);
    }

    public void setFileAppenderEnabled(boolean fileAppenderEnabled) {
      this.fileAppenderEnabled = fileAppenderEnabled;
    }

    public boolean isConsoleAppenderEnabled() {
      return  getEnvWithDefault("LOG_CONSOLE_APPENDER_ENABLED", consoleAppenderEnabled);
    }

    public void setConsoleAppenderEnabled(boolean consoleAppenderEnabled) {
      this.consoleAppenderEnabled = consoleAppenderEnabled;
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

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

package sonia.scm.web.cgi;

import jakarta.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


public interface CGIExecutor {

  String ENV_AUTH_TYPE = "AUTH_TYPE";

  String ENV_CONTENT_LENGTH = "CONTENT_LENGTH";

  String ENV_CONTENT_TYPE = "CONTENT_TYPE";

  String ENV_GATEWAY_INTERFACE = "GATEWAY_INTERFACE";

  String ENV_HTTPS = "HTTPS";

  String ENV_HTTPS_VALUE_OFF = "OFF";

  String ENV_HTTPS_VALUE_ON = "ON";

  String ENV_HTTP_HEADER_PREFIX = "HTTP_";

  String ENV_PATH_INFO = "PATH_INFO";

  String ENV_PATH_TRANSLATED = "PATH_TRANSLATED";

  String ENV_QUERY_STRING = "QUERY_STRING";

  String ENV_REMOTE_ADDR = "REMOTE_ADDR";

  String ENV_REMOTE_HOST = "REMOTE_HOST";

  String ENV_REMOTE_USER = "REMOTE_USER";

  String ENV_REQUEST_METHOD = "REQUEST_METHOD";

  String ENV_SCRIPT_FILENAME = "SCRIPT_FILENAME";

  String ENV_SCRIPT_NAME = "SCRIPT_NAME";

  String ENV_SERVER_NAME = "SERVER_NAME";

  String ENV_SERVER_PORT = "SERVER_PORT";

  String ENV_SERVER_PROTOCOL = "SERVER_PROTOCOL";

  String ENV_SERVER_SOFTWARE = "SERVER_SOFTWARE";

  String ENV_SYSTEM_ROOT = "SystemRoot";

  /**
   * @since 2.12.0
   */
  String RESPONSE_HEADER_CONTENT_TYPE = "Content-Type";

  /**
   * @deprecated use {@link #RESPONSE_HEADER_CONTENT_TYPE} instead.
   */
  @Deprecated
  String REPSONSE_HEADER_CONTENT_TYPE = RESPONSE_HEADER_CONTENT_TYPE;

  String RESPONSE_HEADER_CONTENT_LENGTH = "Content-Length";

  String RESPONSE_HEADER_HTTP_PREFIX = "HTTP";

  String RESPONSE_HEADER_LOCATION = "Location";

  String RESPONSE_HEADER_STATUS = "Status";

  void execute(String cmd) throws IOException, ServletException;



  int getBufferSize();


  EnvList getEnvironment();

  /**
   * @since 1.8
   */
  CGIExceptionHandler getExceptionHandler();


  String getInterpreter();

  /**
   * @since 1.15
   */
  CGIStatusCodeHandler getStatusCodeHandler();


  File getWorkDirectory();

  /**
   * @since 1.12
   */
  boolean isContentLengthWorkaround();


  boolean isIgnoreExitCode();


  boolean isPassShellEnvironment();

  /**
   * Returns command args as list.
   *
   * @since 2.12.0
   */
  default List<String> getArgs() {
    return Collections.emptyList();
  }


  void setBufferSize(int bufferSize);

  /**
   * @since 1.12
   */
  void setContentLengthWorkaround(boolean contentLengthWorkaround);

  void setEnvironment(EnvList environment);

  /**
   * @since 1.8
   */
  void setExceptionHandler(CGIExceptionHandler exceptionHandler);

  void setIgnoreExitCode(boolean ignoreExitCode);

  void setInterpreter(String interpreter);

  void setPassShellEnvironment(boolean passShellEnvironment);

  /**
   * @since 1.15
   */
  void setStatusCodeHandler(CGIStatusCodeHandler statusCodeHandler);

  void setWorkDirectory(File workDirectory);

  /**
   * Set command arguments.
   * @param args command arguments
   * @since 2.12.0
   */
  default void setArgs(List<String> args) {
  }
}

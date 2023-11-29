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

package sonia.scm.web.cgi;

//~--- JDK imports ------------------------------------------------------------

import jakarta.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public interface CGIExecutor {

  /** Field description */
  String ENV_AUTH_TYPE = "AUTH_TYPE";

  /** Field description */
  String ENV_CONTENT_LENGTH = "CONTENT_LENGTH";

  /** Field description */
  String ENV_CONTENT_TYPE = "CONTENT_TYPE";

  /** Field description */
  String ENV_GATEWAY_INTERFACE = "GATEWAY_INTERFACE";

  /** Field description */
  String ENV_HTTPS = "HTTPS";

  /** Field description */
  String ENV_HTTPS_VALUE_OFF = "OFF";

  /** Field description */
  String ENV_HTTPS_VALUE_ON = "ON";

  /** Field description */
  String ENV_HTTP_HEADER_PREFIX = "HTTP_";

  /** Field description */
  String ENV_PATH_INFO = "PATH_INFO";

  /** Field description */
  String ENV_PATH_TRANSLATED = "PATH_TRANSLATED";

  /** Field description */
  String ENV_QUERY_STRING = "QUERY_STRING";

  /** Field description */
  String ENV_REMOTE_ADDR = "REMOTE_ADDR";

  /** Field description */
  String ENV_REMOTE_HOST = "REMOTE_HOST";

  /** Field description */
  String ENV_REMOTE_USER = "REMOTE_USER";

  /** Field description */
  String ENV_REQUEST_METHOD = "REQUEST_METHOD";

  /** Field description */
  String ENV_SCRIPT_FILENAME = "SCRIPT_FILENAME";

  /** Field description */
  String ENV_SCRIPT_NAME = "SCRIPT_NAME";

  /** Field description */
  String ENV_SERVER_NAME = "SERVER_NAME";

  /** Field description */
  String ENV_SERVER_PORT = "SERVER_PORT";

  /** Field description */
  String ENV_SERVER_PROTOCOL = "SERVER_PROTOCOL";

  /** Field description */
  String ENV_SERVER_SOFTWARE = "SERVER_SOFTWARE";

  /** Field description */
  String ENV_SYSTEM_ROOT = "SystemRoot";

  /**
   * Content type header of response.
   * @since 2.12.0
   */
  String RESPONSE_HEADER_CONTENT_TYPE = "Content-Type";

  /**
   * @deprecated use {@link #RESPONSE_HEADER_CONTENT_TYPE} instead.
   */
  @Deprecated
  String REPSONSE_HEADER_CONTENT_TYPE = RESPONSE_HEADER_CONTENT_TYPE;

  /** Field description */
  String RESPONSE_HEADER_CONTENT_LENGTH = "Content-Length";

  /** Field description */
  String RESPONSE_HEADER_HTTP_PREFIX = "HTTP";

  /** Field description */
  String RESPONSE_HEADER_LOCATION = "Location";

  /** Field description */
  String RESPONSE_HEADER_STATUS = "Status";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param cmd
   *
   * @throws IOException
   * @throws ServletException
   */
  void execute(String cmd) throws IOException, ServletException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  int getBufferSize();

  /**
   * Method description
   *
   *
   * @return
   */
  EnvList getEnvironment();

  /**
   * Returns the cgi exception handler.
   *
   *
   * @return cgi exception handler
   * @since 1.8
   */
  CGIExceptionHandler getExceptionHandler();

  /**
   * Method description
   *
   *
   * @return
   */
  String getInterpreter();

  /**
   * Returns the status code handler.
   *
   *
   * @return status code handler
   * @since 1.15
   */
  CGIStatusCodeHandler getStatusCodeHandler();

  /**
   * Method description
   *
   *
   * @return
   */
  File getWorkDirectory();

  /**
   * Method description
   *
   *
   * @return
   * @since 1.12
   */
  boolean isContentLengthWorkaround();

  /**
   * Method description
   *
   *
   * @return
   */
  boolean isIgnoreExitCode();

  /**
   * Method description
   *
   *
   * @return
   */
  boolean isPassShellEnvironment();

  /**
   * Returns command args as list.
   *
   * @return list of command args
   * @since 2.12.0
   */
  default List<String> getArgs() {
    return Collections.emptyList();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param bufferSize
   */
  void setBufferSize(int bufferSize);

  /**
   * Method description
   * @since 1.12
   *
   * @param contentLengthWorkaround
   */
  void setContentLengthWorkaround(boolean contentLengthWorkaround);

  /**
   * Method description
   *
   *
   * @param environment
   */
  void setEnvironment(EnvList environment);

  /**
   * Sets the cgi exception handler.
   *
   *
   * @param exceptionHandler cgi exception handler
   * @since 1.8
   */
  void setExceptionHandler(CGIExceptionHandler exceptionHandler);

  /**
   * Method description
   *
   *
   * @param ignoreExitCode
   */
  void setIgnoreExitCode(boolean ignoreExitCode);

  /**
   * Method description
   *
   *
   * @param interpreter
   */
  void setInterpreter(String interpreter);

  /**
   * Method description
   *
   *
   * @param passShellEnvironment
   */
  void setPassShellEnvironment(boolean passShellEnvironment);

  /**
   * Sets the status code handler.
   *
   *
   * @param statusCodeHandler the handler to set
   * @since 1.15
   */
  void setStatusCodeHandler(CGIStatusCodeHandler statusCodeHandler);

  /**
   * Method description
   *
   *
   * @param workDirectory
   */
  void setWorkDirectory(File workDirectory);

  /**
   * Set command arguments.
   * @param args command arguments
   * @since 2.12.0
   */
  default void setArgs(List<String> args) {
  }
}

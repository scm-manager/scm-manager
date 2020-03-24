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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

/**
 *
 * @author Sebastian Sdorra
 */
public interface CGIExecutor
{

  /** Field description */
  public static final String ENV_AUTH_TYPE = "AUTH_TYPE";

  /** Field description */
  public static final String ENV_CONTENT_LENGTH = "CONTENT_LENGTH";

  /** Field description */
  public static final String ENV_CONTENT_TYPE = "CONTENT_TYPE";

  /** Field description */
  public static final String ENV_GATEWAY_INTERFACE = "GATEWAY_INTERFACE";

  /** Field description */
  public static final String ENV_HTTPS = "HTTPS";

  /** Field description */
  public static final String ENV_HTTPS_VALUE_OFF = "OFF";

  /** Field description */
  public static final String ENV_HTTPS_VALUE_ON = "ON";

  /** Field description */
  public static final String ENV_HTTP_HEADER_PREFIX = "HTTP_";

  /** Field description */
  public static final String ENV_PATH_INFO = "PATH_INFO";

  /** Field description */
  public static final String ENV_PATH_TRANSLATED = "PATH_TRANSLATED";

  /** Field description */
  public static final String ENV_QUERY_STRING = "QUERY_STRING";

  /** Field description */
  public static final String ENV_REMOTE_ADDR = "REMOTE_ADDR";

  /** Field description */
  public static final String ENV_REMOTE_HOST = "REMOTE_HOST";

  /** Field description */
  public static final String ENV_REMOTE_USER = "REMOTE_USER";

  /** Field description */
  public static final String ENV_REQUEST_METHOD = "REQUEST_METHOD";

  /** Field description */
  public static final String ENV_SCRIPT_FILENAME = "SCRIPT_FILENAME";

  /** Field description */
  public static final String ENV_SCRIPT_NAME = "SCRIPT_NAME";

  /** Field description */
  public static final String ENV_SERVER_NAME = "SERVER_NAME";

  /** Field description */
  public static final String ENV_SERVER_PORT = "SERVER_PORT";

  /** Field description */
  public static final String ENV_SERVER_PROTOCOL = "SERVER_PROTOCOL";

  /** Field description */
  public static final String ENV_SERVER_SOFTWARE = "SERVER_SOFTWARE";

  /** Field description */
  public static final String ENV_SYSTEM_ROOT = "SystemRoot";

  /** Field description */
  public static final String REPSONSE_HEADER_CONTENT_TYPE = "Content-Type";

  /** Field description */
  public static final String RESPONSE_HEADER_CONTENT_LENGTH = "Content-Length";

  /** Field description */
  public static final String RESPONSE_HEADER_HTTP_PREFIX = "HTTP";

  /** Field description */
  public static final String RESPONSE_HEADER_LOCATION = "Location";

  /** Field description */
  public static final String RESPONSE_HEADER_STATUS = "Status";

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
  public void execute(String cmd) throws IOException, ServletException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public int getBufferSize();

  /**
   * Method description
   *
   *
   * @return
   */
  public EnvList getEnvironment();

  /**
   * Returns the cgi exception handler.
   *
   *
   * @return cgi exception handler
   * @since 1.8
   */
  public CGIExceptionHandler getExceptionHandler();

  /**
   * Method description
   *
   *
   * @return
   */
  public String getInterpreter();

  /**
   * Returns the status code handler.
   *
   *
   * @return status code handler
   * @since 1.15
   */
  public CGIStatusCodeHandler getStatusCodeHandler();

  /**
   * Method description
   *
   *
   * @return
   */
  public File getWorkDirectory();

  /**
   * Method description
   *
   *
   * @return
   * @since 1.12
   */
  public boolean isContentLengthWorkaround();

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isIgnoreExitCode();

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPassShellEnvironment();

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param bufferSize
   */
  public void setBufferSize(int bufferSize);

  /**
   * Method description
   * @since 1.12
   *
   * @param contentLengthWorkaround
   */
  public void setContentLengthWorkaround(boolean contentLengthWorkaround);

  /**
   * Method description
   *
   *
   * @param environment
   */
  public void setEnvironment(EnvList environment);

  /**
   * Sets the cgi exception handler.
   *
   *
   * @param exceptionHandler cgi exception handler
   * @since 1.8
   */
  public void setExceptionHandler(CGIExceptionHandler exceptionHandler);

  /**
   * Method description
   *
   *
   * @param ignoreExitCode
   */
  public void setIgnoreExitCode(boolean ignoreExitCode);

  /**
   * Method description
   *
   *
   * @param interpreter
   */
  public void setInterpreter(String interpreter);

  /**
   * Method description
   *
   *
   * @param passShellEnvironment
   */
  public void setPassShellEnvironment(boolean passShellEnvironment);

  /**
   * Sets the status code handler.
   *
   *
   * @param statusCodeHandler the handler to set
   * @since 1.15
   */
  public void setStatusCodeHandler(CGIStatusCodeHandler statusCodeHandler);

  /**
   * Method description
   *
   *
   * @param workDirectory
   */
  public void setWorkDirectory(File workDirectory);
}

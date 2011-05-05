/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
   * Method description
   *
   *
   * @return
   */
  public String getInterpreter();

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
   *
   *
   * @param environment
   */
  public void setEnvironment(EnvList environment);

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
   * Method description
   *
   *
   * @param workDirectory
   */
  public void setWorkDirectory(File workDirectory);
}

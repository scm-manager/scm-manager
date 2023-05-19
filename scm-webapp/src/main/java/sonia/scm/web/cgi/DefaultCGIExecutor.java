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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.Authentications;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultCGIExecutor extends AbstractCGIExecutor
{

  /** Field description */
  public static final String CGI_VERSION = "CGI/1.1";

  /** Field description */
  public static final int DEFAULT_BUFFER_SIZE = 16264;

  /** Field description */
  public static final String SYSTEM_ROOT_WINDOWS = "C:\\WINDOWS";

  /** Field description */
  private static final String SERVER_SOFTWARE_PREFIX = "scm-manager/";

  /** the logger for DefaultCGIExecutor */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultCGIExecutor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param executor to handle error stream processing
   * @param configuration
   * @param context
   * @param request
   * @param response
   */
  public DefaultCGIExecutor(ExecutorService executor,
    ScmConfiguration configuration, ServletContext context,
    HttpServletRequest request, HttpServletResponse response)
  {
    this.executor = executor;
    this.configuration = configuration;
    this.context = context;
    this.request = request;
    this.response = response;

    // set default values
    this.bufferSize = DEFAULT_BUFFER_SIZE;
    this.environment = createEnvironment();
  }

  //~--- methods --------------------------------------------------------------

  @Override
  public void execute(String cmd)
  {
    File command = new File(cmd);
    EnvList env = new EnvList(environment);

    if (passShellEnvironment)
    {
      apendOsEnvironment(env);
    }

    if (workDirectory == null)
    {
      workDirectory = command.getParentFile();
    }

    String path = command.getAbsolutePath();
    String pathTranslated = request.getPathTranslated();

    if (Strings.isNullOrEmpty(pathTranslated))
    {
      pathTranslated = path;
    }
    else
    {
      pathTranslated = HttpUtil.removeMatrixParameter(pathTranslated);
    }

    env.set(ENV_PATH_TRANSLATED, pathTranslated);

    List<String> execCmd = new ArrayList<>();
    if (interpreter != null) {
      execCmd.add(interpreter);
    }
    execCmd.add(command.getAbsolutePath());
    execCmd.addAll(getArgs());

    if (logger.isDebugEnabled())
    {
      logger.debug("execute cgi: {}", Joiner.on(' ').join(execCmd));

      if (logger.isTraceEnabled())
      {
        logger.trace(env.toString());
      }
    }

    Process p = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(execCmd);
      builder.directory(workDirectory);
      builder.environment().putAll(env.asMap());
      p = builder.start();
      execute(p);
    }
    catch (IOException ex)
    {
      getExceptionHandler().handleException(request, response, ex);
    }
    finally
    {
      if (p != null)
      {
        p.destroy();
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public CGIExceptionHandler getExceptionHandler()
  {
    if (exceptionHandler == null)
    {
      exceptionHandler = new DefaultCGIExceptionHandler();
    }

    return exceptionHandler;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public CGIStatusCodeHandler getStatusCodeHandler()
  {
    if (statusCodeHandler == null)
    {
      statusCodeHandler = new DefaultCGIStatusCodeHandler();
    }

    return statusCodeHandler;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isContentLengthWorkaround()
  {
    return contentLengthWorkaround;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param contentLengthWorkaround
   */
  @Override
  public void setContentLengthWorkaround(boolean contentLengthWorkaround)
  {
    this.contentLengthWorkaround = contentLengthWorkaround;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param env
   */
  private void apendOsEnvironment(EnvList env)
  {
    Map<String, String> osEnv = System.getenv();

    if (Util.isNotEmpty(osEnv))
    {
      for (Map.Entry<String, String> e : osEnv.entrySet())
      {
        env.set(e.getKey(), e.getValue());
      }
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private EnvList createEnvironment()
  {

    // remove ;jsessionid
    String pathInfo = HttpUtil.removeMatrixParameter(request.getPathInfo());
    String uri = HttpUtil.removeMatrixParameter(request.getRequestURI());
    String scriptName = uri.substring(0, uri.length() - pathInfo.length());
    String scriptPath = context.getRealPath(scriptName);
    EnvList env = new EnvList();

    env.set(ENV_AUTH_TYPE, request.getAuthType());
    env.set(ENV_CONTENT_LENGTH, createCGIContentLength(request, contentLengthWorkaround));

    /*
     * Decode PATH_INFO
     * https://github.com/scm-manager/scm-manager/issues/79
     */
    if (Util.isNotEmpty(pathInfo))
    {
      pathInfo = HttpUtil.decode(pathInfo);
    }

    env.set(ENV_CONTENT_TYPE, Util.nonNull(request.getContentType()));
    env.set(ENV_GATEWAY_INTERFACE, CGI_VERSION);
    env.set(ENV_PATH_INFO, pathInfo);
    env.set(ENV_QUERY_STRING, request.getQueryString());
    env.set(ENV_REMOTE_ADDR, request.getRemoteAddr());
    env.set(ENV_REMOTE_HOST, request.getRemoteHost());

    // The identity information reported about the connection by a
    // RFC 1413 [11] request to the remote agent, if
    // available. Servers MAY choose not to support this feature, or
    // not to request the data for efficiency reasons.
    // "REMOTE_IDENT" => "NYI"
    env.set(ENV_REMOTE_USER, request.getRemoteUser());
    env.set(ENV_REQUEST_METHOD, request.getMethod());
    env.set(ENV_SCRIPT_NAME, scriptName);
    env.set(ENV_SCRIPT_FILENAME, scriptPath);
    env.set(ENV_SERVER_NAME, Util.nonNull(request.getServerName()));

    int serverPort = HttpUtil.getServerPort(configuration, request);

    env.set(ENV_SERVER_PORT, Integer.toString(serverPort));
    env.set(ENV_SERVER_PROTOCOL, Util.nonNull(request.getProtocol()));
    env.set(ENV_SERVER_SOFTWARE,
      SERVER_SOFTWARE_PREFIX.concat(SCMContext.getContext().getVersion()));

    Enumeration<String> enm = request.getHeaderNames();

    while (enm.hasMoreElements())
    {
      String name = enm.nextElement();
      String value = request.getHeader(name);

      env.set(ENV_HTTP_HEADER_PREFIX + name.toUpperCase().replace('-', '_'), value);
    }

    // these extra ones were from printenv on www.dev.nomura.co.uk
    env.set(ENV_HTTPS, (request.isSecure()
      ? ENV_HTTPS_VALUE_ON
      : ENV_HTTPS_VALUE_OFF));

    if (SystemUtil.isWindows())
    {
      env.set(ENV_SYSTEM_ROOT, SYSTEM_ROOT_WINDOWS);
    }

    return env;
  }

  /**
   * Returns the content length as string in the cgi specific format.
   *
   * CGI spec says CONTENT_LENGTH must be NULL ("") or undefined
   * if there is no content, so we cannot put 0 or -1 in as per the
   * Servlet API spec. Some CGI applications require a content
   * length environment variable, which is not null or empty
   * (e.g. mercurial). For those application the disallowEmptyResults
   * parameter should be used.
   *
   * @param disallowEmptyResults {@code true} to return -1 instead of an empty string
   *
   * @return content length as cgi specific string
   */
  @VisibleForTesting
  static String createCGIContentLength(HttpServletRequest request, boolean disallowEmptyResults) {
    String cgiContentLength = disallowEmptyResults ? "-1" : "";

    String contentLength = request.getHeader("Content-Length");
    if (!Strings.isNullOrEmpty(contentLength)) {
      try {
        long len = Long.parseLong(contentLength);
        if (len > 0) {
          cgiContentLength = String.valueOf(len);
        }
      } catch (NumberFormatException ex) {
        logger.warn("received request with invalid content-length header value: {}", contentLength);
      }
    }

    return cgiContentLength;
  }

  /**
   * Method description
   *
   *
   * @param process
   *
   * @throws IOException
   */
  @SuppressWarnings("UnstableApiUsage")
  private void execute(Process process) throws IOException
  {
    InputStream processIS = null;
    ServletOutputStream servletOS = null;

    try
    {
      processErrorStreamAsync(process);
      processServletInput(process);
      processIS = process.getInputStream();
      parseHeaders(processIS);
      servletOS = response.getOutputStream();

      long content = ByteStreams.copy(processIS, servletOS);

      waitForFinish(process, servletOS, content);
    }
    finally
    {
      IOUtil.close(processIS);
      IOUtil.close(servletOS);
    }
  }

  private void parseHeaders(InputStream is) throws IOException {
    String line = null;

    while ((line = getTextLineFromStream(is)).length() > 0) {
      if (logger.isTraceEnabled()) {
        logger.trace("  ".concat(line));
      }

      if (!line.startsWith(RESPONSE_HEADER_HTTP_PREFIX)) {
        int k = line.indexOf(':');

        if (k > 0) {
          String key = line.substring(0, k).trim();
          String value = line.substring(k + 1).trim();

          if (RESPONSE_HEADER_LOCATION.equalsIgnoreCase(key)) {
            response.sendRedirect(response.encodeRedirectURL(value));
          } else if (RESPONSE_HEADER_STATUS.equalsIgnoreCase(key)) {
            handleStatus(value);
          } else {
            // add remaining header items to our response header
            response.addHeader(key, value);
          }
        }
      }
    }
  }

  private void handleStatus(String value) throws IOException {
    String[] token = value.split(" ");
    int status = Integer.parseInt(token[0]);

    logger.debug("CGI returned with status {}", status);

    if (status < 304) {
      response.setStatus(status);
    } else {
      if (status == 401 && Authentications.isAuthenticatedSubjectAnonymous()) {
        HttpUtil.sendUnauthorized(response, configuration.getRealmDescription());
      } else {
        response.sendError(status);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param in
   *
   * @throws IOException
   */
  private void processErrorStream(InputStream in) throws IOException
  {
    if (logger.isWarnEnabled())
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      IOUtil.copy(in, baos);

      if (baos.size() > 0)
      {
        logger.warn(baos.toString());
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param process
   */
  private void processErrorStreamAsync(final Process process)
  {
    executor.execute(() -> {
      InputStream errorStream = null;

      try
      {
        errorStream = process.getErrorStream();
        processErrorStream(errorStream);
      }
      catch (IOException ex)
      {
        logger.error("could not read errorstream", ex);
      }
      finally
      {
        IOUtil.close(errorStream);
      }
    });
  }

  /**
   * Method description
   *
   *
   * @param process
   */
  private void processServletInput(Process process)
  {
    logger.trace("process servlet input");

    OutputStream processOS = null;
    ServletInputStream servletIS = null;

    try
    {
      processOS = process.getOutputStream();
      servletIS = request.getInputStream();
      IOUtil.copy(servletIS, processOS, bufferSize);
    }
    catch (IOException ex)
    {
      logger.error(
        "could not read from ServletInputStream and write to ProcessOutputStream",
        ex);
    }
    finally
    {
      IOUtil.close(processOS);
      IOUtil.close(servletIS);
    }
  }

  /**
   * Method description
   *
   *
   * @param process
   * @param output
   * @param content
   *
   *
   * @throws IOException
   */
  private void waitForFinish(Process process, ServletOutputStream output,
    long content)
    throws IOException
  {
    try
    {
      int exitCode = process.waitFor();

      if (!ignoreExitCode)
      {
        if (logger.isTraceEnabled())
        {
          logger.trace(
            "handle status code {} with statusCodeHandler, there are {} bytes written to outputstream",
            exitCode, content);
        }

        if (content == 0)
        {
          getStatusCodeHandler().handleStatusCode(request, response, output,
            exitCode);
        }
        else
        {
          getStatusCodeHandler().handleStatusCode(request, exitCode);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("ignore status code {}", exitCode);
      }
    }
    catch (InterruptedException ex)
    {
      getExceptionHandler().handleException(request, response, ex);
      Thread.currentThread().interrupt();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param is
   *
   * @return
   *
   * @throws IOException
   */
  private String getTextLineFromStream(InputStream is) throws IOException
  {
    StringBuilder buffer = new StringBuilder();
    int b;

    while ((b = is.read()) != -1 && (b != (int) '\n'))
    {
      buffer.append((char) b);
    }

    return buffer.toString().trim();
  }

  //~--- fields ---------------------------------------------------------------

  /** executor to handle error stream processing */
  private final ExecutorService executor;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private boolean contentLengthWorkaround = false;

  /** Field description */
  private ServletContext context;

  /** Field description */
  private HttpServletRequest request;

  /** Field description */
  private HttpServletResponse response;
}

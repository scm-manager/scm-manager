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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.logging.LoggingOutputStream;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Based on org.eclipse.jetty.servlets.CGI
 *
 * @author Sebastian Sdorra
 *
 */
public class CGIRunner
{

  /** Field description */
  public static final int BUFFERSIZE = 2 * 8192;

  /** Field description */
  private static final Logger logger = LoggerFactory.getLogger(CGIRunner.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param defaultCmdPrefix
   * @param ignoreExitState
   */
  public CGIRunner(ServletContext context, String defaultCmdPrefix,
                   boolean ignoreExitState)
  {
    this.context = context;
    this.defaultCmdPrefix = defaultCmdPrefix;
    this.ignoreExitState = ignoreExitState;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param environment
   * @param command
   * @param pathInfo
   * @param req
   * @param res
   *
   * @throws IOException
   */
  public void exec(EnvList environment, File command, String pathInfo,
                   HttpServletRequest req, HttpServletResponse res, int serverPort)
          throws IOException
  {
    exec(environment, defaultCmdPrefix, command, pathInfo, req, res, serverPort);
  }

  /**
   * Method description
   *
   *
   *
   * @param environment
   * @param cmdPrefix
   * @param command
   * @param pathInfo
   * @param req
   * @param res
   *
   * @throws IOException
   */
  public void exec(EnvList environment, String cmdPrefix, File command,
                   String pathInfo, HttpServletRequest req,
                   HttpServletResponse res, int serverPort)
          throws IOException
  {
    String path = command.getAbsolutePath();
    File dir = command.getParentFile();
    String scriptName = req.getRequestURI().substring(0,
                          req.getRequestURI().length() - pathInfo.length());
    String scriptPath = context.getRealPath(scriptName);
    String pathTranslated = req.getPathTranslated();
    int len = req.getContentLength();

    if (len < 0)
    {
      len = 0;
    }

    if (Util.isEmpty(pathTranslated))
    {
      pathTranslated = path;
    }

    // these ones are from "The WWW Common Gateway Interface Version 1.1"
    // look at :
    // http://Web.Golux.Com/coar/cgi/draft-coar-cgi-v11-03-clean.html#6.1.1
    environment.set("AUTH_TYPE", req.getAuthType());
    environment.set("CONTENT_LENGTH", Integer.toString(len));
    environment.set("CONTENT_TYPE", req.getContentType());
    environment.set("GATEWAY_INTERFACE", "CGI/1.1");
    environment.set("PATH_INFO", pathInfo);
    environment.set("PATH_TRANSLATED", pathTranslated);
    environment.set("QUERY_STRING", req.getQueryString());
    environment.set("REMOTE_ADDR", req.getRemoteAddr());
    environment.set("REMOTE_HOST", req.getRemoteHost());

    // The identity information reported about the connection by a
    // RFC 1413 [11] request to the remote agent, if
    // available. Servers MAY choose not to support this feature, or
    // not to request the data for efficiency reasons.
    // "REMOTE_IDENT" => "NYI"
    environment.set("REMOTE_USER", req.getRemoteUser());
    environment.set("REQUEST_METHOD", req.getMethod());
    environment.set("SCRIPT_NAME", scriptName);
    environment.set("SCRIPT_FILENAME", scriptPath);
    environment.set("SERVER_NAME", req.getServerName());
    environment.set("SERVER_PORT", Integer.toString(serverPort));
    environment.set("SERVER_PROTOCOL", req.getProtocol());
    environment.set("SERVER_SOFTWARE", context.getServerInfo());

    Enumeration enm = req.getHeaderNames();

    while (enm.hasMoreElements())
    {
      String name = (String) enm.nextElement();
      String value = req.getHeader(name);

      environment.set("HTTP_" + name.toUpperCase().replace('-', '_'), value);
    }

    // these extra ones were from printenv on www.dev.nomura.co.uk
    environment.set("HTTPS", (req.isSecure()
                              ? "ON"
                              : "OFF"));

    // "DOCUMENT_ROOT" => root + "/docs",
    // "SERVER_URL" => "NYI - http://us0245",
    // "TZ" => System.getProperty("user.timezone"),
    // are we meant to decode args here ? or does the script get them
    // via PATH_INFO ? if we are, they should be decoded and passed
    // into exec here...
    String execCmd = path;

    if ((execCmd.charAt(0) != '"') && (execCmd.indexOf(" ") >= 0))
    {
      execCmd = "\"" + execCmd + "\"";
    }

    if (cmdPrefix != null)
    {
      execCmd = cmdPrefix + " " + execCmd;
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("execute cgi: ".concat(execCmd));
    }

    Process p = (dir == null)
                ? Runtime.getRuntime().exec(execCmd, environment.getEnvArray())
                : Runtime.getRuntime().exec(execCmd, environment.getEnvArray(),
                  dir);

    // hook processes input to browser's output (async)
    final InputStream inFromReq = req.getInputStream();
    final OutputStream outToCgi = p.getOutputStream();
    final int inLength = len;

    // TODO: print or log error stream
    IOUtil.copyThread(p.getErrorStream(),
                      new LoggingOutputStream(logger,
                        LoggingOutputStream.LEVEL_ERROR));
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          if (inLength > 0)
          {
            copy(inFromReq, outToCgi, inLength);
          }

          outToCgi.close();
        }
        catch (IOException ex)
        {
          logger.debug(ex.getMessage(), ex);
        }
        finally
        {
          IOUtil.close(inFromReq);
          IOUtil.close(outToCgi);
        }
      }
    }).start();

    // hook processes output to browser's input (sync)
    // if browser closes stream, we should detect it and kill process...
    OutputStream os = null;

    try
    {

      // read any headers off the top of our input stream
      // NOTE: Multiline header items not supported!
      String line = null;
      InputStream inFromCgi = p.getInputStream();

      // br=new BufferedReader(new InputStreamReader(inFromCgi));
      // while ((line=br.readLine())!=null)
      while ((line = getTextLineFromStream(inFromCgi)).length() > 0)
      {
        if (!line.startsWith("HTTP"))
        {
          int k = line.indexOf(':');

          if (k > 0)
          {
            String key = line.substring(0, k).trim();
            String value = line.substring(k + 1).trim();

            if ("Location".equals(key))
            {
              res.sendRedirect(res.encodeRedirectURL(value));
            }
            else if ("Status".equals(key))
            {
              String[] token = value.split(" ");
              int status = Integer.parseInt(token[0]);

              res.setStatus(status);
            }
            else
            {

              // add remaining header items to our response header
              res.addHeader(key, value);
            }
          }
        }
      }

      // copy cgi content to response stream...
      os = res.getOutputStream();
      IOUtil.copy(inFromCgi, os);
      p.waitFor();

      if (!ignoreExitState)
      {
        int exitValue = p.exitValue();

        if (0 != exitValue)
        {
          StringBuilder msg = new StringBuilder("Non-zero exit status (");

          msg.append(exitValue).append(") from CGI program: ").append(path);
          logger.warn(msg.toString());

          if (!res.isCommitted())
          {
            res.sendError(500, "Failed to exec CGI");
          }
        }
      }
    }
    catch (IOException e)
    {

      // browser has probably closed its input stream - we
      // terminate and clean up...
      logger.debug("CGI: Client closed connection!");
    }
    catch (InterruptedException ie)
    {
      logger.debug("CGI: interrupted!");
    }
    finally
    {
      if (os != null)
      {
        IOUtil.close(os);
      }

      os = null;
      p.destroy();

      // Log.debug("CGI: terminated!");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getCmdPrefix()
  {
    return defaultCmdPrefix;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ServletContext getContext()
  {
    return context;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isIgnoreExitState()
  {
    return ignoreExitState;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param cmdPrefix
   */
  public void setCmdPrefix(String cmdPrefix)
  {
    this.defaultCmdPrefix = cmdPrefix;
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  public void setContext(ServletContext context)
  {
    this.context = context;
  }

  /**
   * Method description
   *
   *
   * @param ignoreExitState
   */
  public void setIgnoreExitState(boolean ignoreExitState)
  {
    this.ignoreExitState = ignoreExitState;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param in
   * @param out
   * @param byteCount
   *
   * @throws IOException
   */
  private void copy(InputStream in, OutputStream out, long byteCount)
          throws IOException
  {
    byte buffer[] = new byte[BUFFERSIZE];
    int len = BUFFERSIZE;

    if (byteCount >= 0)
    {
      while (byteCount > 0)
      {
        int max = (byteCount < BUFFERSIZE)
                  ? (int) byteCount
                  : BUFFERSIZE;

        len = in.read(buffer, 0, max);

        if (len == -1)
        {
          break;
        }

        byteCount -= len;
        out.write(buffer, 0, len);
      }
    }
    else
    {
      while (true)
      {
        len = in.read(buffer, 0, BUFFERSIZE);

        if (len < 0)
        {
          break;
        }

        out.write(buffer, 0, len);
      }
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

  /** Field description */
  private ServletContext context;

  /** Field description */
  private String defaultCmdPrefix;

  /** Field description */
  private boolean ignoreExitState;
}

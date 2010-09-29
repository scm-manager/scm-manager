/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.cgi;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  private static final Logger logger =
    Logger.getLogger(CGIRunner.class.getName());

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param environment
   * @param cmdPrefix
   * @param ignoreExitState
   */
  public CGIRunner(ServletContext context, EnvList environment,
                   String cmdPrefix, boolean ignoreExitState)
  {
    this.context = context;
    this.environment = environment;
    this.cmdPrefix = cmdPrefix;
    this.ignoreExitState = ignoreExitState;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param command
   * @param pathInfo
   * @param req
   * @param res
   *
   * @throws IOException
   */
  public void exec(File command, String pathInfo, HttpServletRequest req,
                   HttpServletResponse res)
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

    if ((pathTranslated == null) || (pathTranslated.length() == 0))
    {
      pathTranslated = path;
    }

    EnvList env = new EnvList(environment);

    // these ones are from "The WWW Common Gateway Interface Version 1.1"
    // look at :
    // http://Web.Golux.Com/coar/cgi/draft-coar-cgi-v11-03-clean.html#6.1.1
    env.set("AUTH_TYPE", req.getAuthType());
    env.set("CONTENT_LENGTH", Integer.toString(len));
    env.set("CONTENT_TYPE", req.getContentType());
    env.set("GATEWAY_INTERFACE", "CGI/1.1");

    if ((pathInfo != null) && (pathInfo.length() > 0))
    {
      env.set("PATH_INFO", pathInfo);
    }

    env.set("PATH_TRANSLATED", pathTranslated);
    env.set("QUERY_STRING", req.getQueryString());
    env.set("REMOTE_ADDR", req.getRemoteAddr());
    env.set("REMOTE_HOST", req.getRemoteHost());

    // The identity information reported about the connection by a
    // RFC 1413 [11] request to the remote agent, if
    // available. Servers MAY choose not to support this feature, or
    // not to request the data for efficiency reasons.
    // "REMOTE_IDENT" => "NYI"
    env.set("REMOTE_USER", req.getRemoteUser());
    env.set("REQUEST_METHOD", req.getMethod());
    env.set("SCRIPT_NAME", scriptName);
    env.set("SCRIPT_FILENAME", scriptPath);
    env.set("SERVER_NAME", req.getServerName());
    env.set("SERVER_PORT", Integer.toString(req.getServerPort()));
    env.set("SERVER_PROTOCOL", req.getProtocol());
    env.set("SERVER_SOFTWARE", context.getServerInfo());

    Enumeration enm = req.getHeaderNames();

    while (enm.hasMoreElements())
    {
      String name = (String) enm.nextElement();
      String value = req.getHeader(name);

      env.set("HTTP_" + name.toUpperCase().replace('-', '_'), value);
    }

    // these extra ones were from printenv on www.dev.nomura.co.uk
    env.set("HTTPS", (req.isSecure()
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

    if (logger.isLoggable(Level.FINE))
    {
      logger.fine("execute cgi: ".concat(execCmd));
    }

    Process p = (dir == null)
                ? Runtime.getRuntime().exec(execCmd, env.getEnvArray())
                : Runtime.getRuntime().exec(execCmd, env.getEnvArray(), dir);

    // hook processes input to browser's output (async)
    final InputStream inFromReq = req.getInputStream();
    final OutputStream outToCgi = p.getOutputStream();
    final int inLength = len;

    // TODO: print or log error stream
    IOUtil.copyThread(new InputStreamReader(p.getErrorStream()),
                      new OutputStreamWriter(System.err));
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
        catch (IOException e)
        {
          logger.log(Level.FINEST, null, e);
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
          logger.warning(msg.toString());

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
      logger.finest("CGI: Client closed connection!");
    }
    catch (InterruptedException ie)
    {
      logger.finest("CGI: interrupted!");
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
  private String cmdPrefix;

  /** Field description */
  private ServletContext context;

  /** Field description */
  private EnvList environment;

  /** Field description */
  private boolean ignoreExitState;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.cgi;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CGI Servlet.
 *
 * The cgi bin directory can be set with the "cgibinResourceBase" init parameter
 * or it will default to the resource base of the context.
 *
 * The "commandPrefix" init parameter may be used to set a prefix to all
 * commands passed to exec. This can be used on systems that need assistance to
 * execute a particular file type. For example on windows this can be set to
 * "perl" so that perl scripts are executed.
 *
 * The "Path" init param is passed to the exec environment as PATH. Note: Must
 * be run unpacked somewhere in the filesystem.
 *
 * Any initParameter that starts with ENV_ is used to set an environment
 * variable with the name stripped of the leading ENV_ and using the init
 * parameter value.
 *
 * Based on org.eclipse.jetty.servlets.CGI
 *
 * @author Sebastian Sdorra
 *
 */
public class CGIServlet extends HttpServlet
{

  /** Field description */
  private static final long serialVersionUID = 5719539505555835833L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException
  {
    EnvList env = new EnvList();
    String cmdPrefix = getInitParameter("commandPrefix");
    boolean ignoreExitStatus =
      "true".equalsIgnoreCase(getInitParameter("ignoreExitState"));
    String commandPath = getInitParameter("command");

    if (Util.isNotEmpty(commandPath))
    {
      command = new File(commandPath);
    }

    Enumeration e = getInitParameterNames();

    while (e.hasMoreElements())
    {
      String n = (String) e.nextElement();

      if ((n != null) && n.startsWith("ENV_"))
      {
        env.set(n.substring(4), getInitParameter(n));
      }
    }

    if (!env.containsKey("SystemRoot"))
    {
      String os = System.getProperty("os.name");

      if ((os != null) && (os.toLowerCase().indexOf("windows") != -1))
      {
        env.set("SystemRoot", "C:\\WINDOWS");
      }
    }

    cgiRunner = new CGIRunner(getServletContext(), env, cmdPrefix,
                              ignoreExitStatus);
  }

  /**
   * Method description
   *
   *
   * @param req
   * @param resp
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException
  {
    cgiRunner.exec(command, req.getPathInfo(), req, resp);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CGIRunner cgiRunner;

  /** Field description */
  private File command;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.cgi;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractCGIServlet extends HttpServlet
{

  /** Field description */
  private static final long serialVersionUID = -8638099037069714140L;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param req
   *
   * @return
   *
   * @throws IOException
   * @throws ServletException
   */
  protected abstract File getCommand(HttpServletRequest req)
          throws ServletException, IOException;

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
    cgiRunner = new CGIRunner(getServletContext(), createEnvironment(),
                              getCmdPrefix(), isExitStateIgnored());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  protected EnvList createEnvironment()
  {
    EnvList env = new EnvList();
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

    return env;
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
    cgiRunner.exec(getCommand(req), req.getPathInfo(), req, resp);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected String getCmdPrefix()
  {
    return null;
  }

  ;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected boolean isExitStateIgnored()
  {
    return false;
  }

  ;

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CGIRunner cgiRunner;
}

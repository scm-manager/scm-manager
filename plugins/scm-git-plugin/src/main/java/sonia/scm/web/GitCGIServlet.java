/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import sonia.scm.web.cgi.AbstractCGIServlet;
import sonia.scm.web.cgi.EnvList;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class GitCGIServlet extends AbstractCGIServlet
{

  /** Field description */
  public static final String ENV_HTTP_EXPORT_ALL = "GIT_HTTP_EXPORT_ALL";

  /** Field description */
  public static final String ENV_PROJECT_ROOT = "GIT_PROJECT_ROOT";

  /** Field description */
  private static final long serialVersionUID = 9147517765161830847L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected EnvList createEnvironment()
  {
    EnvList list = super.createEnvironment();

    list.set(ENV_PROJECT_ROOT, "/tmp/git");
    list.set(ENV_HTTP_EXPORT_ALL, "");

    return list;
  }

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
  @Override
  protected File getCommand(HttpServletRequest req)
          throws ServletException, IOException
  {
    return new File("/opt/local/libexec/git-core/git-http-backend/");
  }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
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
   *
   * @throws ServletException
   */
  @Override
  protected EnvList createBaseEnvironment() throws ServletException
  {
    EnvList list = super.createBaseEnvironment();

    list.set(ENV_HTTP_EXPORT_ALL, "");

    return list;
  }

  /**
   * Method description
   *
   *
   * @param baseEnvironment
   *
   * @return
   *
   * @throws ServletException
   */
  @Override
  protected EnvList createRequestEnvironment(EnvList baseEnvironment)
          throws ServletException
  {
    GitConfig config = repositoryHandler.getConfig();

    if (config == null)
    {
      throw new ServletException("git is not configured");
    }

    EnvList env = new EnvList(baseEnvironment);

    env.set(ENV_PROJECT_ROOT, config.getRepositoryDirectory().getPath());

    return env;
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
    GitConfig config = repositoryHandler.getConfig();

    if (config == null)
    {
      throw new ServletException("git is not configured");
    }

    return new File(config.getGitHttpBackend());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private GitRepositoryHandler repositoryHandler;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.servlet.ServletModule;

import sonia.scm.web.filter.BasicAuthenticationFilter;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitWebPlugin implements ScmWebPlugin
{

  /** Field description */
  public static final String SCRIPT = "/sonia/scm/git.config.js";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void contextDestroyed(ScmWebPluginContext context)
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void contextInitialized(ScmWebPluginContext context)
  {
    context.addScriptResource(new ClasspathWebResource(SCRIPT));
    context.addInjectModule(new ServletModule()
    {
      @Override
      protected void configureServlets()
      {
        filter("/git/*").through(BasicAuthenticationFilter.class);
        serve("/git/*").with(GitCGIServlet.class);
      }
    });
  }
}

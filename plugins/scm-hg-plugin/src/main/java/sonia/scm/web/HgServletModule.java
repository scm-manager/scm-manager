/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import sonia.scm.web.filter.BasicAuthenticationFilter;
import sonia.scm.web.cgi.CGIServlet;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgServletModule extends ServletModule
{

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  HgServletModule(ServletContext servletContext)
  {
    cgiPath = HgUtil.getCGI().getAbsolutePath();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    filter("/hg/*").through(BasicAuthenticationFilter.class);

    Map<String, String> initParams = new HashMap<String, String>();

    initParams.put("command", cgiPath);
    bind(CGIServlet.class).in(Scopes.SINGLETON);
    serve("/hg/*").with(CGIServlet.class, initParams);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String cgiPath;
}

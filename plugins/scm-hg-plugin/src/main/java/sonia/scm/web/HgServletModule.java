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
public class HgServletModule extends ServletModule
{

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    filter("/hg/*").through(BasicAuthenticationFilter.class);
    serve("/hg/*").with(HgCGIServlet.class);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String cgiPath;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import org.apache.catalina.servlets.CGIServlet;

import sonia.scm.web.filter.BasicAuthenticationFilter;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

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

    Map<String, String> initParams = new HashMap<String, String>();

    initParams.put("cgiPathPrefix", "WEB-INF/cgi/hgweb.cgi");
    bind(CGIServlet.class).in(Scopes.SINGLETON);
    serve("/hg/*").with(CGIServlet.class, initParams);
  }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Module;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmWebPluginContext
{

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  public ScmWebPluginContext(ServletContext servletContext)
  {
    this.servletContext = servletContext;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param module
   */
  public void addInjectModule(Module module)
  {
    injectModules.add(module);
  }

  /**
   * Method description
   *
   *
   * @param resource
   */
  public void addScriptResource(WebResource resource)
  {
    scriptResources.add(resource);
  }

  /**
   * Method description
   *
   *
   * @param module
   */
  public void removeInjectModule(Module module)
  {
    injectModules.remove(module);
  }

  /**
   * Method description
   *
   *
   * @param resource
   */
  public void removeScriptResource(WebResource resource)
  {
    scriptResources.remove(resource);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<Module> getInjectModules()
  {
    return injectModules;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<WebResource> getScriptResources()
  {
    return scriptResources;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ServletContext getServletContext()
  {
    return servletContext;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<WebResource> scriptResources = new HashSet<WebResource>();

  /** Field description */
  private Set<Module> injectModules = new HashSet<Module>();

  /** Field description */
  private ServletContext servletContext;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.plugin;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URL;

import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class SCMPluginManager
{

  /** Field description */
  public static final String PATH_PLUGINCONFIG = "META-INF/scm/plugin.xml";

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(SCMPluginManager.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  public void load() throws IOException
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      classLoader = SCMPluginManager.class.getClassLoader();
    }

    load(classLoader);
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   *
   * @throws IOException
   */
  public void load(ClassLoader classLoader) throws IOException
  {
    Enumeration<URL> urlEnum = classLoader.getResources(PATH_PLUGINCONFIG);

    if (urlEnum != null)
    {
      while (urlEnum.hasMoreElements())
      {
        URL url = urlEnum.nextElement();

        loadPlugin(url);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<SCMPlugin> getPlugins()
  {
    return plugins;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   */
  private void loadPlugin(URL url)
  {
    try
    {
      SCMPlugin plugin = JAXB.unmarshal(url, SCMPlugin.class);

      if (logger.isInfoEnabled())
      {
        logger.info("load plugin {}", url.toExternalForm());
      }

      plugins.add(plugin);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage(), ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<SCMPlugin> plugins = new LinkedHashSet<SCMPlugin>();
}

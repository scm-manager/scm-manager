/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import javax.servlet.ServletContextEvent;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sebastian Sdorra
 */
public class ContextListener extends GuiceServletContextListener
{

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent)
  {
    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.ALL);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    logger.addHandler( handler );
    super.contextInitialized(servletContextEvent);
  }



  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Injector getInjector()
  {
    ScmWebPluginContext webPluginContext = new ScmWebPluginContext();
    List<ScmWebPlugin> plugins = webPluginContext.getPlugins();
    List<Module> modules = new ArrayList<Module>();

    modules.add(new ScmServletModule(webPluginContext));

    if (Util.isNotEmpty(plugins))
    {
      for (ScmWebPlugin plugin : plugins)
      {
        Module[] moduleArray = plugin.getModules();

        if (Util.isNotEmpty(moduleArray))
        {
          modules.addAll(Arrays.asList(moduleArray));
        }
      }
    }

    return Guice.createInjector(modules);
  }
}

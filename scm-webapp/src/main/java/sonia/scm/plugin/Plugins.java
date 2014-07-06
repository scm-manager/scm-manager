/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.nio.file.Path;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public final class Plugins
{

  /**
   * Constructs ...
   *
   */
  private Plugins() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param directory
   *
   * @return
   *
   * @throws IOException
   */
  public static Set<PluginWrapper> collectPlugins(ClassLoader classLoader,
    Path directory)
    throws IOException
  {
    PluginProcessor processor = new PluginProcessor(directory);

    return processor.collectPlugins(classLoader);
  }

  /**
   * Method description
   *
   *
   * @param wrapped
   *
   * @return
   */
  public static Iterable<Plugin> unwrap(Iterable<PluginWrapper> wrapped)
  {
    return Iterables.transform(wrapped, new Unwrap());
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/06/05
   * @author         Enter your name here...
   */
  private static class Unwrap implements Function<PluginWrapper, Plugin>
  {

    /**
     * Method description
     *
     *
     * @param wrapper
     *
     * @return
     */
    @Override
    public Plugin apply(PluginWrapper wrapper)
    {
      return wrapper.getPlugin();
    }
  }
}

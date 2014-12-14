/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URL;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultPluginLoader implements PluginLoader
{

  /** Field description */
  public static final String PATH_MODULECONFIG = "META-INF/scm/module.xml";

  /** Field description */
  public static final String PATH_PLUGINCONFIG = "META-INF/scm/plugin.xml";

  /** the logger for DefaultPluginLoader */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginLoader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param servletContext
   * @param parent
   * @param installedPlugins
   */
  public DefaultPluginLoader(ServletContext servletContext, ClassLoader parent,
    Set<PluginWrapper> installedPlugins)
  {
    this.installedPlugins = installedPlugins;
    this.uberClassLoader = new UberClassLoader(parent, installedPlugins);
    this.uberWebResourceLoader =
      new DefaultUberWebResourceLoader(servletContext, installedPlugins);

    try
    {
      JAXBContext context = JAXBContext.newInstance(ScmModule.class,
                              Plugin.class);

      modules = getInstalled(parent, context, PATH_MODULECONFIG);

      appendExtensions(multiple, single, extensions, modules);
      appendExtensions(multiple, single, extensions, unwrap());
    }
    catch (IOException | JAXBException ex)
    {
      throw Throwables.propagate(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param binder
   */
  @Override
  public void processExtensions(Binder binder)
  {
    logger.info("start processing extensions");

    if (logger.isInfoEnabled())
    {
      logger.info(
        "found {} extensions for {} multiple and {} single extension points",
        extensions.size(), multiple.size(), single.size());
    }

    new ExtensionBinder(binder).bind(multiple, single, extensions);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<Module> getInjectionModules()
  {
    return ImmutableSet.copyOf(injectionModules);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<ScmModule> getInstalledModules()
  {
    return modules;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginWrapper> getInstalledPlugins()
  {
    return installedPlugins;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public ClassLoader getUberClassLoader()
  {
    return uberClassLoader;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public UberWebResourceLoader getUberWebResourceLoader()
  {
    return uberWebResourceLoader;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param multiple
   * @param single
   * @param extensions
   * @param mods
   */
  private void appendExtensions(Set<Class> multiple, Set<Class> single,
    Set<Class> extensions, Iterable<? extends ScmModule> mods)
  {
    for (ScmModule mod : mods)
    {
      for (ExtensionPointElement epe : mod.getExtensionPoints())
      {
        if (epe.isMultiple())
        {
          multiple.add(epe.getClazz());
        }
        else
        {
          single.add(epe.getClazz());
        }
      }

      for (Class extensionClass : mod.getExtensions())
      {
        if (Module.class.isAssignableFrom(extensionClass))
        {
          try
          {
            injectionModules.add((Module) extensionClass.newInstance());
          }
          catch (IllegalAccessException | InstantiationException ex)
          {
            logger.error("could not create instance of module", ex);
          }
        }
        else
        {
          extensions.add(extensionClass);
        }
      }

      Iterables.addAll(extensions, mod.getRestProviders());
      Iterables.addAll(extensions, mod.getRestResources());
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Iterable<Plugin> unwrap()
  {
    return PluginsInternal.unwrap(installedPlugins);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param context
   * @param path
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   * @throws JAXBException
   */
  @SuppressWarnings("unchecked")
  private <T> Set<T> getInstalled(ClassLoader classLoader, JAXBContext context,
    String path)
    throws IOException, JAXBException
  {
    Builder<T> builder = ImmutableSet.builder();
    Enumeration<URL> urls = classLoader.getResources(path);

    while (urls.hasMoreElements())
    {
      URL url = urls.nextElement();
      T module = (T) context.createUnmarshaller().unmarshal(url);

      builder.add(module);
    }

    return builder.build();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ClassLoader uberClassLoader;

  /** Field description */
  private final UberWebResourceLoader uberWebResourceLoader;

  /** Field description */
  private Set<PluginWrapper> installedPlugins;

  /** Field description */
  private Set<ScmModule> modules;

  /** Field description */
  private Set<Class> multiple = Sets.newHashSet();

  /** Field description */
  private Set<Class> single = Sets.newHashSet();

  /** Field description */
  private Set<Module> injectionModules = Sets.newHashSet();

  /** Field description */
  private Set<Class> extensions = Sets.newHashSet();
}

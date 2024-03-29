/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.plugin;


import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import jakarta.servlet.ServletContext;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;


public class DefaultPluginLoader implements PluginLoader
{

  public static final String PATH_MODULECONFIG = "META-INF/scm/module.xml";

  public static final String PATH_PLUGINCONFIG = "META-INF/scm/plugin.xml";

  private final ExtensionProcessor extensionProcessor;

  private final Set<InstalledPlugin> installedPlugins;

  private final Set<ScmModule> modules;

  private final ClassLoader uberClassLoader;

  private final UberWebResourceLoader uberWebResourceLoader;

  public DefaultPluginLoader(ServletContext servletContext, ClassLoader parent,
    Set<InstalledPlugin> installedPlugins, ConfigurationResolver configurationResolver)
  {
    this.installedPlugins = installedPlugins;
    this.uberClassLoader = new UberClassLoader(parent, installedPlugins);
    this.uberWebResourceLoader =
      new DefaultUberWebResourceLoader(servletContext, installedPlugins);

    try
    {
      JAXBContext context = JAXBContext.newInstance(ScmModule.class,
                              InstalledPluginDescriptor.class);

      modules = getInstalled(parent, context, PATH_MODULECONFIG);

      ExtensionCollector collector = new ExtensionCollector(parent, modules, installedPlugins);
      extensionProcessor = new DefaultExtensionProcessor(collector, configurationResolver);
    }
    catch (IOException | JAXBException ex)
    {
      throw Throwables.propagate(ex);
    }
  }


  
  @Override
  public ExtensionProcessor getExtensionProcessor()
  {
    return extensionProcessor;
  }

  
  @Override
  public Collection<ScmModule> getInstalledModules()
  {
    return modules;
  }

  
  @Override
  public Collection<InstalledPlugin> getInstalledPlugins()
  {
    return installedPlugins;
  }

  
  @Override
  public ClassLoader getUberClassLoader()
  {
    return uberClassLoader;
  }

  
  @Override
  public UberWebResourceLoader getUberWebResourceLoader()
  {
    return uberWebResourceLoader;
  }


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

}

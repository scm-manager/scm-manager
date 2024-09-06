/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

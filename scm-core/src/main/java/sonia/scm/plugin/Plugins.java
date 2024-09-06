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


import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Util methods to handle plugins.
 *
 * @since 2.0.0
 */
public final class Plugins
{

  private static JAXBContext context;

  static
  {
    try
    {
      context = JAXBContext.newInstance(InstalledPluginDescriptor.class, ScmModule.class);
    }
    catch (JAXBException ex)
    {
      throw new PluginException("could not create jaxb context", ex);
    }
  }


  private Plugins() {}


  public static InstalledPluginDescriptor parsePluginDescriptor(Path path)
  {
    return parsePluginDescriptor(Files.asByteSource(path.toFile()));
  }

  public static InstalledPluginDescriptor parsePluginDescriptor(ByteSource data)
  {
    Preconditions.checkNotNull(data, "data parameter is required");

    InstalledPluginDescriptor plugin;

    try (InputStream stream = data.openStream())
    {
      plugin = (InstalledPluginDescriptor) context.createUnmarshaller().unmarshal(stream);
    }
    catch (JAXBException ex)
    {
      throw new PluginLoadException("could not parse plugin descriptor", ex);
    }
    catch (IOException ex)
    {
      throw new PluginLoadException("could not read plugin descriptor", ex);
    }

    return plugin;
  }
}

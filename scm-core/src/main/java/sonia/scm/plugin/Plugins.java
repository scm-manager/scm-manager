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

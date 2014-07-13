/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Util methods to handle plugins.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class Plugins
{

  /** Field description */
  private static JAXBContext context;

  //~--- static initializers --------------------------------------------------

  static
  {
    try
    {
      context = JAXBContext.newInstance(Plugin.class, ScmModule.class);
    }
    catch (JAXBException ex)
    {
      throw new PluginException("could not create jaxb context", ex);
    }
  }

  //~--- constructors ---------------------------------------------------------

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
   * @param path
   *
   * @return
   */
  public static Plugin parsePluginDescriptor(Path path)
  {
    return parsePluginDescriptor(Files.asByteSource(path.toFile()));
  }

  /**
   * Method description
   *
   *
   * @param data
   *
   * @return
   */
  public static Plugin parsePluginDescriptor(ByteSource data)
  {
    Preconditions.checkNotNull(data, "data parameter is required");

    Plugin plugin;

    try (InputStream stream = data.openStream())
    {
      plugin = (Plugin) context.createUnmarshaller().unmarshal(stream);
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

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



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.junit.rules.TemporaryFolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.Iterator;

/**
 *
 * @author Sebastian Sdorra
 */
public class CacheConfigurationTestLoader implements CacheConfigurationLoader
{

  /**
   * Constructs ...
   *
   *
   * @param tempFolder
   */
  public CacheConfigurationTestLoader(TemporaryFolder tempFolder)
  {
    this.tempFolder = tempFolder;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param tempFolder
   * @param defaultConfiguration
   */
  public CacheConfigurationTestLoader(TemporaryFolder tempFolder,
    String defaultConfiguration)
  {
    this.tempFolder = tempFolder;
    this.defaultConfiguration = defaultConfiguration;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param tempFolder
   * @param defaultConfiguration
   * @param moduleConfigurations
   */
  public CacheConfigurationTestLoader(TemporaryFolder tempFolder,
    String defaultConfiguration, Iterator<String> moduleConfigurations)
  {
    this.tempFolder = tempFolder;
    this.defaultConfiguration = defaultConfiguration;
    this.moduleConfigurations = moduleConfigurations;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param tempFolder
   * @param defaultConfiguration
   * @param moduleConfigurations
   * @param manualConfiguration
   */
  public CacheConfigurationTestLoader(TemporaryFolder tempFolder,
    String defaultConfiguration, Iterator<String> moduleConfigurations,
    String manualConfiguration)
  {
    this.tempFolder = tempFolder;
    this.defaultConfiguration = defaultConfiguration;
    this.moduleConfigurations = moduleConfigurations;
    this.manualConfiguration = manualConfiguration;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public URL getDefaultResource()
  {
    URL url = null;

    if (defaultConfiguration != null)
    {
      url = getResource(defaultConfiguration);
    }

    return url;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public File getManualFileResource()
  {
    File file;

    if (manualConfiguration == null)
    {
      file = mock(File.class);
      when(file.exists()).thenReturn(Boolean.FALSE);
    }
    else
    {
      try
      {
        file = tempFolder.newFile();

        URL manual = getResource(manualConfiguration);

        Files.copy(Resources.newInputStreamSupplier(manual), file);
      }
      catch (IOException ex)
      {
        throw new RuntimeException("could not create manual config file", ex);
      }
    }

    return file;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<URL> getModuleResources()
  {
    Iterator<URL> urlIterator;

    if (moduleConfigurations == null)
    {
      urlIterator = Iterators.emptyIterator();
    }
    else
    {
      urlIterator = Iterators.transform(moduleConfigurations,
        new Function<String, URL>()
      {

        @Override
        public URL apply(String resource)
        {
          return getResource(resource);
        }
      });
    }

    return urlIterator;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  private URL getResource(String name)
  {
    return Resources.getResource("sonia/scm/cache/".concat(name));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String defaultConfiguration;

  /** Field description */
  private String manualConfiguration;

  /** Field description */
  private Iterator<String> moduleConfigurations;

  /** Field description */
  private TemporaryFolder tempFolder;
}

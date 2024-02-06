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
    
package sonia.scm.cache;


import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import static java.util.Collections.emptyIterator;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CacheConfigurationTestLoader implements CacheConfigurationLoader
{

 
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

        ByteSource source = Resources.asByteSource(manual);
        source.copyTo(Files.asByteSink(file));
      }
      catch (IOException ex)
      {
        throw new RuntimeException("could not create manual config file", ex);
      }
    }

    return file;
  }

  
  @Override
  public Iterator<URL> getModuleResources()
  {
    Iterator<URL> urlIterator;

    if (moduleConfigurations == null)
    {
      urlIterator = emptyIterator();
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


  private URL getResource(String name)
  {
    return Resources.getResource("sonia/scm/cache/".concat(name));
  }

  //~--- fields ---------------------------------------------------------------

  private String defaultConfiguration;

  private String manualConfiguration;

  private Iterator<String> moduleConfigurations;

  private TemporaryFolder tempFolder;
}

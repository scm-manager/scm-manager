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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Iterators;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.plugin.PluginLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import static java.util.Collections.emptyIterator;

public class DefaultCacheConfigurationLoader implements CacheConfigurationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultCacheConfigurationLoader.class);

  private static final String DEFAULT = "/config/gcache.xml";
  private static final String MANUAL_RESOURCE = "ext".concat(File.separator).concat("gcache.xml");
  private static final String MODULE_RESOURCES = "META-INF/scm/gcache.xml";

  private final ClassLoader classLoader;

  @Inject
  public DefaultCacheConfigurationLoader(PluginLoader pluginLoader) {
    this(pluginLoader.getUberClassLoader());
  }

  public DefaultCacheConfigurationLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public URL getDefaultResource() {
    return DefaultCacheConfigurationLoader.class.getResource(DEFAULT);
  }

  @Override
  public File getManualFileResource() {
    return new File(SCMContext.getContext().getBaseDirectory(), MANUAL_RESOURCE);
  }

  @Override
  public Iterator<URL> getModuleResources() {
    try {
      Enumeration<URL> enm = classLoader.getResources(MODULE_RESOURCES);
      if (enm != null) {
        return Iterators.forEnumeration(enm);
      }
    } catch (IOException ex) {
      LOG.error("could not read module resources", ex);
    }
    return emptyIterator();
  }

}

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

package sonia.scm.cache;


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

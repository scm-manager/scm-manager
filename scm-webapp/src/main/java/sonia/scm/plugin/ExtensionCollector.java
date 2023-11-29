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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ConfigElement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ExtensionCollector {

  private static final Logger LOG = LoggerFactory.getLogger(ExtensionCollector.class);

  private final Set<String> pluginIndex;

  private final Set<WebElementExtension> webElements = Sets.newHashSet();
  private final Set<Class<?>> indexedTypes = Sets.newHashSet();
  private final Set<Class> restResources = Sets.newHashSet();
  private final Set<Class> restProviders = Sets.newHashSet();
  private final Set<Class> mappers = Sets.newHashSet();
  private final Set<Class> looseExtensions = Sets.newHashSet();

  private final Set<ConfigElement> configElements = Sets.newHashSet();
  private final Multimap<ExtensionPointElement, Class> extensions = HashMultimap.create();
  private final Map<Class, ExtensionPointElement> extensionPointIndex = Maps.newHashMap();

  public ExtensionCollector(ClassLoader moduleClassLoader, Set<ScmModule> modules, Set<InstalledPlugin> installedPlugins) {
    this.pluginIndex = createPluginIndex(installedPlugins);

    for (ScmModule module : modules) {
      collectRootElements(moduleClassLoader, module);
    }

    for (InstalledPlugin plugin : installedPlugins) {
      collectRootElements(plugin.getClassLoader(), plugin.getDescriptor());
    }

    for (ScmModule module : modules) {
      collectExtensions(moduleClassLoader, module);
    }

    for (InstalledPlugin plugin : installedPlugins) {
      collectExtensions(plugin.getClassLoader(), plugin.getDescriptor());
    }
  }

  private Set<String> createPluginIndex(Set<InstalledPlugin> installedPlugins) {
    return installedPlugins.stream()
      .map(p -> p.getDescriptor().getInformation().getName())
      .collect(Collectors.toSet());
  }

  public Collection<Class> byExtensionPoint(ExtensionPointElement epe) {
    Collection<Class> collection = extensions.get(epe);

    if (collection == null) {
      collection = Collections.emptySet();
    }

    return collection;
  }

  public Collection<Class> byExtensionPoint(Class clazz) {
    Collection<Class> exts;
    ExtensionPointElement epe = extensionPointIndex.get(clazz);

    if (epe != null) {
      exts = byExtensionPoint(epe);
    } else {
      exts = Collections.emptySet();
    }

    return exts;
  }

  public Class oneByExtensionPoint(ExtensionPointElement epe) {
    return Iterables.getFirst(byExtensionPoint(epe), null);
  }

  public Class oneByExtensionPoint(Class clazz) {
    return Iterables.getFirst(byExtensionPoint(clazz), null);
  }

  public Iterable<ExtensionPointElement> getExtensionPointElements() {
    return extensionPointIndex.values();
  }

  public Multimap<ExtensionPointElement, Class> getExtensions() {
    return extensions;
  }

  public Set<Class> getLooseExtensions() {
    return looseExtensions;
  }

  public Set<Class> getRestProviders() {
    return restProviders;
  }

  public Set<Class> getRestResources() {
    return restResources;
  }

  public Set<Class> getMappers() {
    return mappers;
  }

  public Set<ConfigElement> getConfigElements() {
    return configElements;
  }

  public Set<WebElementExtension> getWebElements() {
    return webElements;
  }

  public Set<Class<?>> getIndexedTypes() {
    return indexedTypes;
  }

  private void appendExtension(Class extension) {
    boolean found = false;

    for (Entry<Class, ExtensionPointElement> e : extensionPointIndex.entrySet()) {
      if (e.getKey().isAssignableFrom(extension)) {
        extensions.put(e.getValue(), extension);
        found = true;

        break;
      }
    }

    if (!found) {
      looseExtensions.add(extension);
    }
  }

  private void collectExtensions(ClassLoader defaultClassLoader, ScmModule module) {
    for (ClassElement extension : module.getExtensions()) {
      if (isRequirementFulfilled(extension)) {
        Class<?> extensionClass = load(defaultClassLoader, extension);
        appendExtension(extensionClass);
      }
    }
  }

  private Set<Class<?>> collectClasses(ClassLoader defaultClassLoader, Iterable<ClassElement> classElements) {
    Set<Class<?>> classes = new HashSet<>();
    for (ClassElement element : classElements) {
      if (isRequirementFulfilled(element)) {
        Class<?> loadedClass = load(defaultClassLoader, element);
        classes.add(loadedClass);
      }
    }
    return classes;
  }

  private Collection<Class<?>> collectIndexedTypes(ClassLoader defaultClassLoader, Iterable<ClassElement> descriptors) {
    Set<Class<?>> types = new HashSet<>();
    for (ClassElement descriptor : descriptors) {
      Class<?> loadedClass = load(defaultClassLoader, descriptor);
      types.add(loadedClass);
    }
    return types;
  }

  private Set<WebElementExtension> collectWebElementExtensions(ClassLoader defaultClassLoader, Iterable<WebElementDescriptor> descriptors) {
    Set<WebElementExtension> webElementExtensions = new HashSet<>();
    for (WebElementDescriptor descriptor : descriptors) {
      if (isRequirementFulfilled(descriptor)) {
        Class<?> loadedClass = load(defaultClassLoader, descriptor);
        webElementExtensions.add(new WebElementExtension(loadedClass, descriptor));
      }
    }
    return webElementExtensions;
  }

  private Class<?> load(ClassLoader classLoader, ClassElement extension) {
    try {
      return classLoader.loadClass(extension.getClazz());
    } catch (ClassNotFoundException ex) {
      throw new PluginLoadException("failed to load clazz", ex);
    }
  }

  private boolean isRequirementFulfilled(ClassElement extension) {
    if (extension.getRequires() != null) {
      for (String requiredPlugin : extension.getRequires()) {
        if (!pluginIndex.contains(requiredPlugin)) {
          LOG.debug("skip loading of extension {}, because the required plugin {} is not installed", extension.getClazz(), requiredPlugin);
          return false;
        }
      }
    }
    return true;
  }

  private void collectRootElements(ClassLoader classLoader, ScmModule module) {
    for (ExtensionPointElement epe : module.getExtensionPoints()) {
      extensionPointIndex.put(epe.getClazz(), epe);
    }

    restProviders.addAll(collectClasses(classLoader, module.getRestProviders()));
    restResources.addAll(collectClasses(classLoader, module.getRestResources()));
    mappers.addAll(collectClasses(classLoader, module.getMappers()));

    webElements.addAll(collectWebElementExtensions(classLoader, module.getWebElements()));
    indexedTypes.addAll(collectIndexedTypes(classLoader, module.getIndexedTypes()));
    Iterables.addAll(configElements, module.getConfigElements());
  }
}

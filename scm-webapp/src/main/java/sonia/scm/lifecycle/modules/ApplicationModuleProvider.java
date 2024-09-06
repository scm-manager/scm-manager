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

package sonia.scm.lifecycle.modules;

import com.google.common.base.Throwables;
import com.google.inject.Module;
import jakarta.servlet.ServletContext;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.Stage;
import sonia.scm.api.v2.resources.MapperModule;
import sonia.scm.debug.DebugModule;
import sonia.scm.filter.WebElementModule;
import sonia.scm.plugin.ExtensionProcessor;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.ExecutorModule;
import sonia.scm.validation.ValidationModule;

import java.util.ArrayList;
import java.util.List;

public class ApplicationModuleProvider implements ModuleProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationModuleProvider.class);

  private final ServletContext servletContext;
  private final PluginLoader pluginLoader;

  public ApplicationModuleProvider(ServletContext servletContext, PluginLoader pluginLoader) {
    this.servletContext = servletContext;
    this.pluginLoader = pluginLoader;
  }

  @Override
  public List<Module> createModules() {
    ClassOverrides overrides = createClassOverrides();
    return createModules(overrides);
  }

  private List<Module> createModules(ClassOverrides overrides) {
    List<Module> moduleList = new ArrayList<>();
    moduleList.add(new ValidationModule());
    moduleList.add(new ResteasyModule());
    moduleList.add(ShiroWebModule.guiceFilterModule());
    moduleList.add(new WebElementModule(pluginLoader));
    moduleList.add(new ConfigModule(pluginLoader));
    moduleList.add(new ScmServletModule(pluginLoader, overrides));
    moduleList.add(
      new ScmSecurityModule(servletContext, pluginLoader.getExtensionProcessor())
    );
    appendModules(pluginLoader.getExtensionProcessor(), moduleList);
    moduleList.addAll(overrides.getModules());

    if (SCMContext.getContext().getStage() == Stage.DEVELOPMENT){
      moduleList.add(new DebugModule());
    }
    moduleList.add(new MapperModule());
    moduleList.add(new ExecutorModule());
    moduleList.add(new WorkingCopyPoolModule(pluginLoader));

    return moduleList;
  }

  private ClassOverrides createClassOverrides() {
    ClassLoader uberClassLoader = pluginLoader.getUberClassLoader();
    return ClassOverrides.findOverrides(uberClassLoader);
  }

  private void appendModules(ExtensionProcessor ep, List<Module> moduleList) {
    for (Class<? extends Module> module : ep.byExtensionPoint(Module.class)) {
      try {
        LOG.info("add module {}", module);
        moduleList.add(module.newInstance());
      } catch (IllegalAccessException | InstantiationException ex) {
        throw Throwables.propagate(ex);
      }
    }
  }

}

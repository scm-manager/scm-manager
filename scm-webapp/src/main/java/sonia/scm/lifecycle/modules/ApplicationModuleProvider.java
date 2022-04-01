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

package sonia.scm.lifecycle.modules;

import com.google.common.base.Throwables;
import com.google.inject.Module;
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

import javax.servlet.ServletContext;
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

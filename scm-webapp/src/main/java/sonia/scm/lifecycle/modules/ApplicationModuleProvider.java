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

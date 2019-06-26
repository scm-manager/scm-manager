package sonia.scm.lifecycle.modules;

import com.google.inject.Module;

import java.util.Collection;

public interface ModuleProvider {

  Collection<Module> createModules();

}

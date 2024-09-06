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

type Module = {
  dependencies: string[];
  fn: (...args: unknown[]) => Module;
};

class ModuleResolutionError extends Error {
  constructor(module: string) {
    super("Could not resolve module: " + module);
  }
}

const modules: { [name: string]: unknown } = {};
const lazyModules: { [name: string]: () => Promise<unknown> } = {};
const queue: { [name: string]: Module } = {};
const bundleLoaderPromises: {
  [name: string]: { resolve: (module: unknown) => void; reject: (reason?: unknown) => void };
} = {};

export const defineLazy = (name: string, cmp: () => Promise<unknown>) => {
  lazyModules[name] = cmp;
};

export const defineStatic = (name: string, cmp: unknown) => {
  modules[name] = cmp;
};

/**
 * Attempt to retrieve a module from the registry.
 *
 * If a lazy module is requested, it will be loaded and then returned after adding it to the registry.
 *
 * @see defineLazy
 * @see defineStatic
 * @see defineModule
 * @throws {ModuleResolutionError} If the requested module cannot be found/loaded
 */
const resolveModule = async (name: string) => {
  const module = modules[name];
  if (module) {
    return module;
  }

  const lazyModuleLoader = lazyModules[name];
  if (lazyModuleLoader) {
    const lazyModule = await lazyModuleLoader();
    modules[name] = lazyModule;
    return lazyModule;
  }

  throw new ModuleResolutionError(name);
};

/**
 * Executes a module and attempts to resolve all of its dependencies.
 *
 * If a dependency is not (yet) present, the module loading is deferred.
 *
 * Once a module on which the given module depends loaded successfully, it will
 * kickstart another attempt at loading the given module.
 */
const defineModule = async (name: string, module: Module) => {
  try {
    const resolvedDependencies = await Promise.all(module.dependencies.map(resolveModule));
    const loadedModuleName = "@scm-manager/" + name;

    // Store module to be used as dependency for other modules
    modules[loadedModuleName] = module.fn(...resolvedDependencies);

    // Resolve bundle in which module was defined
    if (name in bundleLoaderPromises) {
      bundleLoaderPromises[name].resolve(modules[loadedModuleName]);
      delete bundleLoaderPromises[name];
    }

    // Executed queued modules that depend on the loaded module
    for (const [queuedModuleName, queuedModule] of Object.entries(queue)) {
      if (queuedModule.dependencies.includes(loadedModuleName)) {
        delete queue[queuedModuleName];
        defineModule(queuedModuleName, queuedModule).then();
      }
    }
  } catch (reason) {
    if (reason instanceof ModuleResolutionError) {
      // Wait for module dependency to load
      queue[name] = module;
    } else if (name in bundleLoaderPromises) {
      // Forward error to bundle loader in which module was defined
      bundleLoaderPromises[name].reject(reason);
      delete bundleLoaderPromises[name];
    } else {
      // Throw unhandled exception
      throw reason;
    }
  }
};

/**
 * This is attached to the global window scope and is automatically executed when a plugin module bundle is loaded.
 *
 * @see https://github.com/amdjs/amdjs-api/blob/master/AMD.md
 */
export const define = (name: string, dependencies: string[], fn: (...args: unknown[]) => Module) => {
  defineModule(name, { dependencies, fn }).then();
};

/**
 * As amd modules are loaded asynchronously using the global {@link define} function,
 * we need to register a callback for the loader to notify us when the bundle has been loaded and executed.
 * This has to be done **BEFORE** the bundle's javascript is loaded.
 */
export const registerModuleLoadingCallback = (
  moduleName: string,
  resolve: (value: unknown) => void,
  reject: (reason?: unknown) => void
) => (bundleLoaderPromises[moduleName] = { resolve, reject });

// This module has a side effect and is required to be imported unconditionally into the application at all times.
declare global {
  interface Window {
    define: typeof define;
  }
}

window.define = define;

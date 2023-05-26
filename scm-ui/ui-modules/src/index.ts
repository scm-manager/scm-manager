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
 * Asynchronously loads and executes a given resource bundle.
 *
 * If a module name is supplied, the bundle is expected to contain a single (AMD)[https://github.com/amdjs/amdjs-api/blob/master/AMD.md]
 * module matching the provided module name.
 *
 * The promise will only resolve once the bundle loaded and, if it is a module,
 * all dependencies are resolved and the module executed.
 */
export const load = (resource: string, moduleName?: string) =>
  new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = resource;

    if (moduleName) {
      bundleLoaderPromises[moduleName] = { resolve, reject };
    } else {
      script.onload = resolve;
    }

    script.onerror = reject;

    const body = document.querySelector("body");
    body?.appendChild(script);
    body?.removeChild(script);
  });

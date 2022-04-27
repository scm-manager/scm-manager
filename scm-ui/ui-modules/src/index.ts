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

export const defineLazy = (name: string, cmp: () => Promise<unknown>) => {
  lazyModules[name] = cmp;
};

export const defineStatic = (name: string, cmp: unknown) => {
  modules[name] = cmp;
};

const resolveModule = (name: string) => {
  const module = modules[name];
  if (module) {
    return Promise.resolve(module);
  }

  const lazyModule = lazyModules[name];
  if (lazyModule) {
    return lazyModule().then((mod: unknown) => {
      modules[name] = mod;
      return mod;
    });
  }

  return Promise.reject(new ModuleResolutionError(name));
};

const defineModule = (name: string, module: Module) => {
  Promise.all(module.dependencies.map(resolveModule))
    .then((resolvedDependencies) => {
      modules["@scm-manager/" + name] = module.fn(...resolvedDependencies);

      Object.keys(queue).forEach((queuedModuleName) => {
        const queueModule = queue[queuedModuleName];
        delete queue[queuedModuleName];
        defineModule(queuedModuleName, queueModule);
      });
    })
    .catch((e) => {
      if (e instanceof ModuleResolutionError) {
        queue[name] = module;
      } else {
        throw e;
      }
    });
};

export const define = (name: string, dependencies: string[], fn: (...args: unknown[]) => Module) => {
  defineModule(name, { dependencies, fn });
};

export const load = (resource: string) => {
  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = resource;
    script.onload = resolve;
    script.onerror = reject;

    const body = document.querySelector("body");
    body?.appendChild(script);
    body?.removeChild(script);
  });
};

type Module = {
  dependencies: string[];
  fn: (...args: any[]) => any;
};

const modules: { [name: string]: any } = {};
const lazyModules: { [name: string]: any } = {};
const queue: { [name: string]: Module } = {};

export const defineLazy = (name: string, cmp: any) => {
  lazyModules[name] = cmp;
};

export const defineStatic = (name: string, cmp: any) => {
  modules[name] = cmp;
};

const resolveModule = (name: string) => {
  const module = modules[name];
  if (module) {
    return Promise.resolve(module);
  }

  const lazyModule = lazyModules[name];
  if (lazyModule) {
    return lazyModule().then((mod: any) => {
      modules[name] = mod;
      return mod;
    });
  }

  return Promise.reject("Could not resolve module: " + name);
};

const defineModule = (name: string, module: Module) => {
  Promise.all(module.dependencies.map(resolveModule))
    .then(resolvedDependencies => {
      delete queue[name];

      modules["@scm-manager/" + name] = module.fn(...resolvedDependencies);

      Object.keys(queue).forEach(queuedModuleName => {
        const queueModule = queue[queuedModuleName];
        defineModule(queuedModuleName, queueModule);
      });
    })
    .catch(() => {
      queue[name] = module;
    });
};

export const define = (name: string, dependencies: string[], fn: (...args: any[]) => any) => {
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

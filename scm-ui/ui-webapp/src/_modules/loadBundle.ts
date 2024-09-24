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

import { registerModuleLoadingCallback } from "./define";

/**
 * Asynchronously loads and executes a given resource bundle.
 *
 * If a module name is supplied, the bundle is expected to contain a single (AMD)[https://github.com/amdjs/amdjs-api/blob/master/AMD.md]
 * module matching the provided module name.
 *
 * The promise will only resolve once the bundle loaded and, if it is a module,
 * all dependencies are resolved and the module executed.
 */
export default (resource: string, moduleName?: string) =>
  new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = resource;

    if (moduleName) {
      registerModuleLoadingCallback(moduleName, resolve, reject);
    } else {
      script.onload = resolve;
    }

    script.onerror = reject;

    const body = document.querySelector("body");
    body?.appendChild(script);
    body?.removeChild(script);
  });

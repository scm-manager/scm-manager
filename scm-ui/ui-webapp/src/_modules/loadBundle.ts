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

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

import "./prismConfig";
import { refractor } from "refractor/lib/core";
// @ts-ignore e have no types fpr json import
import aliases from "./mapping.json";
import dependencies from "./dependencies";

type RunHookEnv = {
  classes: string[];
};

export type RefractorAdapter = typeof refractor & {
  isLanguageRegistered: (lang: string) => boolean;
  loadLanguage: (lang: string, callback: () => void) => void;
};

const createAdapter = (theme: { [key: string]: string }): RefractorAdapter => {
  const isLanguageRegistered = (lang: string) => {
    const registeredLanguages = refractor.listLanguages();
    return registeredLanguages.includes(lang);
  };

  const loadLanguageWithDependencies = async (alias: string) => {
    const lang = aliases[alias] || alias;
    const deps = dependencies[lang] || [];
    for (const dep of deps) {
      await loadLanguageWithDependencies(dep);
    }

    if (isLanguageRegistered(lang)) {
      return Promise.resolve();
    } else {
      try {
        const loadedLanguage = await import(/* webpackChunkName: "sh-lang-[request]" */ `refractor/lang/${lang}.js`);
        refractor.register(loadedLanguage.default);
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(`failed to load refractor language ${lang}: ${e}`);
      }
    }
  };

  const loadLanguage = async (alias: string, callback: () => void) => {
    await loadLanguageWithDependencies(alias);
    callback();
  };

  // @ts-ignore hooks are not in the type definition
  const originalRunHook = refractor.hooks.run;
  // @ts-ignore hooks are not in the type definition
  refractor.hooks.run = (name: string, env: RunHookEnv) => {
    originalRunHook.apply(name, env);
    if (env.classes) {
      env.classes = env.classes.map((className) => theme[className] || className);
    }
  };

  return {
    isLanguageRegistered,
    loadLanguage,
    ...refractor,
  };
};

export default createAdapter;

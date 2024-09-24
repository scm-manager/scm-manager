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

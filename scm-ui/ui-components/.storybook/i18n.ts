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

import i18n from "i18next";
import { initReactI18next } from "react-i18next";

type Translation = Record<string, unknown>;

// The translations of the webapp are the single source of truth, the "plugins" namespace is contributed
// by the backend. Both are bundled eagerly, so stories render with the real texts right away and
// switching the language does not require a backend.
const files: Record<string, Translation> = {
  ...import.meta.glob<Translation>("../../ui-webapp/public/locales/*/*.json", { eager: true, import: "default" }),
  ...import.meta.glob<Translation>("../../../scm-webapp/src/main/resources/locales/*/*.json", {
    eager: true,
    import: "default",
  }),
};

const resources: Record<string, Record<string, Translation>> = {};
Object.entries(files).forEach(([path, translation]) => {
  const match = path.match(/\/locales\/([^/]+)\/([^/]+)\.json$/);
  if (!match) {
    return;
  }
  const [, language, namespace] = match;
  resources[language] = { ...resources[language], [namespace]: translation };
});

export const languages = Object.keys(resources).sort();
const namespaces = Array.from(new Set(languages.flatMap((language) => Object.keys(resources[language]))));

i18n.use(initReactI18next).init({
  lng: "en",
  fallbackLng: "en",

  // try to load only "en" and not "en_US"
  load: "languageOnly",

  ns: namespaces,
  defaultNS: "commons",
  debug: false,

  interpolation: {
    escapeValue: false,
  },

  resources,
});

export default i18n;

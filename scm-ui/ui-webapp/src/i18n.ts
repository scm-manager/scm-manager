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
import Backend from "i18next-fetch-backend";
import LanguageDetector from "i18next-browser-languagedetector";
import { initReactI18next } from "react-i18next";
import { urls } from "@scm-manager/ui-components";

const loadPath = urls.withContextPath("/locales/{{lng}}/{{ns}}.json");

i18n
  .use(Backend)
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: "en",

    // try to load only "en" and not "en_US"
    load: "languageOnly",

    // have a common namespace used around the full app
    ns: ["commons"],
    defaultNS: "commons",

    debug: false,

    interpolation: {
      escapeValue: false, // not needed for react!!
    },

    react: {
      useSuspense: false,
    },

    backend: {
      loadPath: loadPath,
      init: {
        credentials: "same-origin",
      },
    },

    // configure LanguageDetector
    // see https://github.com/i18next/i18next-browser-languageDetector#detector-options
    detection: {
      // we only use browser configuration
      order: ["navigator"],
      // we do not cache the detected language
      caches: [],
    },
  });

export default i18n;

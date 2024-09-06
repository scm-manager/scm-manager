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

import i18next from "i18next";
import { initReactI18next } from "react-i18next";
import { withI18next } from "storybook-addon-i18next";
import React, {useEffect} from "react";
import withApiProvider from "./withApiProvider";
import { withThemes } from 'storybook-addon-themes/react';

let i18n = i18next;

// only use fetch backend for storybook
// and not for storyshots
if (!process.env.JEST_WORKER_ID) {
  const Backend = require("i18next-fetch-backend");
  i18n = i18n.use(Backend.default);
}

i18n.use(initReactI18next).init({
  whitelist: ["en", "de", "es"],
  lng: "en",
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
  react: {
    useSuspense: false,
  },
  backend: {
    loadPath: "/locales/{{lng}}/{{ns}}.json",
    init: {
      credentials: "same-origin",
    },
  },
});

export const decorators = [
  withI18next({
    i18n,
    languages: {
      en: "English",
      de: "Deutsch",
      es: "Spanisch",
    },
  }),
  withApiProvider,
  withThemes
];

const Decorator = ({children, themeName}) => {
  useEffect(() => {
    const link = document.querySelector("#ui-theme");
    if (link && link["data-theme"] !== themeName) {
      link.href = `ui-theme-${themeName}.css`;
      link["data-theme"] = themeName;
    }
  }, [themeName]);
  return <>{children}</>
};

export const parameters = {
  actions: { argTypesRegex: "^on[A-Z].*" },
  themes: {
    Decorator,
    clearable: false,
    default: "light",
    list: [
      { name: "light", color: "#fff" },
      { name: "highcontrast", color: "#050514" },
      { name: "dark", color: "#121212" },
    ],
  }
};

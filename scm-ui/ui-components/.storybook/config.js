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
import i18next from "i18next";
import { initReactI18next } from "react-i18next";
import { addDecorator, configure } from "@storybook/react";
import { withI18next } from "storybook-addon-i18next";
import "!style-loader!css-loader!sass-loader!../../ui-styles/src/scm.scss";
import React from "react";
import withApiProvider from "./withApiProvider";

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
    escapeValue: false
  },
  react: {
    useSuspense: false
  },
  backend: {
    loadPath: "/locales/{{lng}}/{{ns}}.json",
    init: {
      credentials: "same-origin"
    }
  }
});

addDecorator(
  withI18next({
    i18n,
    languages: {
      en: "English",
      de: "Deutsch",
      es: "Spanisch"
    }
  })
);

addDecorator(withApiProvider);

configure(require.context("../src", true, /\.stories\.tsx?$/), module);

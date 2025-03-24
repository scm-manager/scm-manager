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

import { initReactI18next } from "react-i18next";
import i18n from "i18next";

/**
 * This provides a minimum i18next scaffold during initialization of a unit test.
 *
 * It does not connect to the i18next information used in production,
 * but avoids warnings emerging due to i18next being uninitialized.
 *
 * More information: <a href="https://react.i18next.com/misc/testing">https://react.i18next.com/misc/testing</a>
 */
export function stubI18Next() {
  // TODO should be changed to async/await
  i18n.use(initReactI18next).init({
    lng: "de",
    fallbackLng: "en",

    ns: ["translationsNS"],
    defaultNS: "translationsNS",

    debug: false,

    interpolation: {
      escapeValue: false,
    },

    resources: { en: { translationsNS: {} } },
  });
}

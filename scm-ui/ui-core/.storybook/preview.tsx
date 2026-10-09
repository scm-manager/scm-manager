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

import type { Preview, Decorator } from "@storybook/react";
import * as React from "react";
import { Suspense, useEffect } from "react";
import { I18nextProvider } from "react-i18next";
import i18n, { languages } from "./i18n";
import { MemoryRouter } from "react-router-dom";

// @ts-ignore
import lightStyle from "@ui-styles/light.scss?url";
// @ts-ignore
import darkStyle from "@ui-styles/dark.scss?url";
// @ts-ignore
import highContrastStyle from "@ui-styles/highcontrast.scss?url";

const LANGUAGE_TITLES: Record<string, string> = {
  en: "English",
  de: "Deutsch",
  es: "Español",
};

const THEMES: Record<string, string> = {
  light: lightStyle,
  dark: darkStyle,
  highcontrast: highContrastStyle,
};

const withI18next: Decorator = (Story, context) => {
  const { locale } = context.globals;

  useEffect(() => {
    if (locale) {
      i18n.changeLanguage(locale);
    }
  }, [locale]);

  return (
    <Suspense fallback={<div>loading translations...</div>}>
      <I18nextProvider i18n={i18n}>
        <Story />
      </I18nextProvider>
    </Suspense>
  );
};

function setTheme(theme: string) {
  const id = "storybook-theme-stylesheet";
  let linkEl = document.getElementById(id) as HTMLLinkElement | null;

  if (!linkEl) {
    linkEl = document.createElement("link");
    linkEl.id = id;
    linkEl.rel = "stylesheet";
    document.head.appendChild(linkEl);
  }

  linkEl.href = THEMES[theme];
}

export const withTheme: Decorator = (Story, context) => {
  const theme = context.globals.theme || "light";

  const linkEl = document.getElementById("theme") as HTMLLinkElement | null;
  if (linkEl) {
    linkEl.setAttribute("href", THEMES[theme]);
    linkEl.setAttribute("data-theme", theme);
  }

  return <Story />;
};

const withRouter: Decorator = (Story, context) => (
  <MemoryRouter key={context.id} initialEntries={["/"]}>
    <Story />
  </MemoryRouter>
);

const preview: Preview = {
  decorators: [withRouter, withI18next, withTheme],
  globalTypes: {
    locale: {
      name: "Locale",
      description: "Internationalization locale",
      toolbar: {
        icon: "globe",
        items: languages.map((language) => ({ value: language, title: LANGUAGE_TITLES[language] ?? language })),
        showName: true,
      },
    },
    theme: {
      name: "Theme",
      description: "Global theme for components",
      toolbar: {
        icon: "paintbrush",
        items: [
          { value: "light", title: "Light" },
          { value: "dark", title: "Dark" },
          { value: "highcontrast", title: "High Contrast" },
        ],
        showName: true,
      },
    },
  },
  parameters: {
    actions: { argTypesRegex: "^on[A-Z].*" },
    storySort: {
      method: "alphabetical",
      order: [],
    },
  },
};

export default preview;

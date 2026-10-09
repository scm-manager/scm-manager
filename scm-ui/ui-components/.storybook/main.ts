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

import { mergeConfig } from "vite";
import path from "path";

import { createRequire } from "module";
import { fileURLToPath } from "url";

const require = createRequire(import.meta.url);
const currentDir = path.dirname(fileURLToPath(import.meta.url));

const config = {
  stories: ["../src/**/*.stories.@(ts|tsx)"],
  addons: ["@storybook/addon-a11y", "@storybook/addon-docs", "@storybook/addon-links", "msw-storybook-addon"],
  framework: {
    name: "@storybook/react-vite",
    options: {},
  },
  typescript: {
    reactDocgen: false,
  },
  async viteFinal(config: any) {
    return mergeConfig(config, {
      publicDir: path.resolve(currentDir, "../../ui-webapp/public"),
      resolve: {
        alias: {
          "decode-named-character-reference": require.resolve("decode-named-character-reference"),
          "react-query/devtools": require.resolve("@tanstack/react-query-devtools"),
          "@ui-styles": path.resolve(currentDir, "../../ui-styles/src"),
          "refractor/lang": path.join(path.dirname(require.resolve("refractor")), "lang"),
        },
      },
      plugins: [],
    });
  },
};

export default config;

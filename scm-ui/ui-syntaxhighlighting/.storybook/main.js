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

module.exports = {
  stories: ["../src/**/*.stories.@(ts|tsx)"],
  addons: [
    {
      name: "@storybook/addon-essentials",
      options: {
        actions: false
      }
    }
  ],
  typescript: { reactDocgen: false },
  staticDirs: ["../static"],
  framework: "@storybook/react",
  core: {
    builder: "webpack5"
  },
  webpackFinal: async config => {
    // force node version of "decode-named-character-reference" instead of browser version which does not work in web worker
    config.resolve.alias["decode-named-character-reference"] = require.resolve("decode-named-character-reference");
    return config;
  }
};

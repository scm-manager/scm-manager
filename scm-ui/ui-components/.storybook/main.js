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

const path = require("path");
const fs = require("fs");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const RemoveThemesPlugin = require("./RemoveThemesPlugin");
const ReactDOM = require("react-dom");

const root = path.resolve("..");

const themedir = path.join(root, "ui-styles", "src");

ReactDOM.createPortal = (node) => node;

const themes = fs
  .readdirSync(themedir)
  .map((filename) => path.parse(filename))
  .filter((p) => p.ext === ".scss")
  .reduce((entries, current) => ({ ...entries, [`ui-theme-${current.name}`]: path.join(themedir, current.base) }), {});
// .map(f => path.join(themedir, f.base));

module.exports = {
  core: {
    builder: "webpack5",
  },
  typescript: { reactDocgen: false },
  stories: ["../src/**/*.stories.tsx"],
  framework: "@storybook/react",
  addons: ["storybook-addon-i18next", "storybook-addon-themes", "@storybook/addon-links", "@storybook/addon-essentials", "@storybook/addon-interactions"],
  webpackFinal: async (config) => {
    // add our themes to webpack entry points
    config.entry = {
      main: config.entry,
      ...themes,
    };

    // create separate css files for our themes
    config.plugins.push(
      new MiniCssExtractPlugin({
        filename: "[name].css",
        ignoreOrder: false,
      })
    );

    config.module.rules.push({
      test: /\.scss$/,
      use: [MiniCssExtractPlugin.loader, "css-loader", "sass-loader"],
    });

    // the html-webpack-plugin adds the generated css and js files to the iframe,
    // which overrides our manually loaded css files.
    // So we use a custom plugin which uses a hook of html-webpack-plugin
    // to filter our themes from the output.
    config.plugins.push(new RemoveThemesPlugin());

    // force node version of "decode-named-character-reference" instead of browser version which does not work in web worker
    config.resolve.alias["decode-named-character-reference"] = require.resolve("decode-named-character-reference");

    // force cjs instead of esm
    // https://github.com/tannerlinsley/react-query/issues/3513
    config.resolve.alias["react-query/devtools"] = require.resolve("react-query/devtools");

    return config;
  },
};

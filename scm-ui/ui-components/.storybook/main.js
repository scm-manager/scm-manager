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

const path = require("path");
const fs = require("fs");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const RemoveThemesPlugin = require("./RemoveThemesPlugin");
const WorkerPlugin = require("worker-plugin");
const ReactDOM = require("react-dom");

const root = path.resolve("..");

const themedir = path.join(root, "ui-styles", "src");

ReactDOM.createPortal = node => node;

const themes = fs
  .readdirSync(themedir)
  .map(filename => path.parse(filename))
  .filter(p => p.ext === ".scss")
  .reduce((entries, current) => ({ ...entries, [`ui-theme-${current.name}`]: path.join(themedir, current.base) }), {});
// .map(f => path.join(themedir, f.base));

module.exports = {
  core: {
    builder: "webpack5"
  },
  typescript: { reactDocgen: false },
  stories: ["../src/**/*.stories.tsx"],
  addons: ["storybook-addon-i18next", "storybook-addon-themes"],
  webpackFinal: async config => {
    // add our themes to webpack entry points
    config.entry = {
      main: config.entry,
      ...themes
    };

    // create separate css files for our themes
    config.plugins.push(
      new MiniCssExtractPlugin({
        filename: "[name].css",
        ignoreOrder: false
      })
    );

    config.module.rules.push({
      test: /\.scss$/,
      use: [MiniCssExtractPlugin.loader, "css-loader", "sass-loader"]
    });

    // the html-webpack-plugin adds the generated css and js files to the iframe,
    // which overrides our manually loaded css files.
    // So we use a custom plugin which uses a hook of html-webpack-plugin
    // to filter our themes from the output.
    config.plugins.push(new RemoveThemesPlugin());

    return config;
  }
};

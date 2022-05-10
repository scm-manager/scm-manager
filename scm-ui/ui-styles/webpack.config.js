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
const HtmlWebpackPlugin = require("html-webpack-plugin");

const themes = fs
  .readdirSync("src")
  .map((filename) => path.parse(filename))
  .filter((p) => p.ext === ".scss")
  .reduce((entries, current) => ({ ...entries, [current.name]: `./src/${current.base}` }), {});

const plugins = Object.keys(themes).map(
  (theme) =>
    new HtmlWebpackPlugin({
      filename: `${theme}.html`,
      template: "./public/_theme.html",
      inject: false,
      theme,
    })
);

plugins.push(
  new HtmlWebpackPlugin({
    filename: "index.html",
    template: "./public/_index.html",
    inject: false,
    themes: Object.keys(themes),
  })
);

module.exports = {
  mode: "development",
  entry: themes,
  devtool: "eval-cheap-module-source-map",
  target: "web",
  module: {
    rules: [
      {
        test: /\.(css|scss|sass)$/i,
        use: [
          // Creates `style` nodes from JS strings
          "style-loader",
          // Translates CSS into CommonJS
          "css-loader",
          // Compiles Sass to CSS
          "sass-loader",
        ],
      },
      {
        test: /\.(png|svg|jpg|gif)$/,
        use: ["file-loader"],
      },
    ],
  },
  output: {
    filename: "theme-[name].bundle.js",
  },
  plugins,
  devServer: {
    static: [
      {
        directory: path.join(__dirname, "public"),
        publicPath: "/",
      },
      {
        directory: path.join(__dirname, "..", "ui-webapp", "public"),
        publicPath: "/ui-webapp",
      },
    ],
    port: 5000,
    client: {
      overlay: true,
    },
    hot: true,
  },
};

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
        use: ["file-loader"]
      }
    ]
  },
  output: {
    filename: "theme-[name].bundle.js",
  },
  plugins,
  devServer: {
    static: [{
      directory: path.join(__dirname, "public"),
      publicPath: "/",
    }, {
      directory: path.join(__dirname, "..", "ui-webapp", "public"),
      publicPath: "/ui-webapp",
    }],
    port: 5000,
    client: {
      overlay: true,
    },
    hot: true,
  },
};

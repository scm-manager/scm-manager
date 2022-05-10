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

const root = process.cwd();

const packageJsonPath = path.join(root, "package.json");
const packageJSON = JSON.parse(fs.readFileSync(packageJsonPath, { encoding: "UTF-8" }));

let { name } = packageJSON;
const orgaIndex = name.indexOf("/");
if (orgaIndex > 0) {
  name = name.substring(orgaIndex + 1);
}

module.exports = function (mode) {
  return {
    context: root,
    entry: {
      [name]: [path.resolve(__dirname, "webpack-public-path.js"), packageJSON.main || "src/main/js/index.js"],
    },
    mode,
    stats: "minimal",
    devtool: "source-map",
    target: "web",
    node: {
      fs: "empty",
      net: "empty",
      tls: "empty",
    },
    externals: [
      "react",
      "react-dom",
      "react-i18next",
      "react-router-dom",
      "styled-components",
      "@scm-manager/ui-types",
      "@scm-manager/ui-extensions",
      "@scm-manager/ui-components",
      "classnames",
      "query-string",
      "redux",
      "react-redux",
      /^@scm-manager\/scm-.*-plugin$/i,
    ],
    module: {
      rules: [
        {
          test: /\.(js|ts|jsx|tsx)$/i,
          exclude: /node_modules/,
          use: {
            loader: "babel-loader",
            options: {
              presets: ["@scm-manager/babel-preset"],
            },
          },
        },
        {
          test: /\.(css|scss|sass)$/i,
          use: ["style-loader", "css-loader", "sass-loader"],
        },
        {
          test: /\.(png|svg|jpg|gif|woff2?|eot|ttf)$/,
          use: ["file-loader"],
        },
      ],
    },
    resolve: {
      extensions: [".ts", ".tsx", ".js", ".jsx", ".css", ".scss", ".json"],
    },
    output: {
      path: path.join(root, "target", `${name}-${packageJSON.version}`, "webapp", "assets"),
      filename: "[name].bundle.js",
      chunkFilename: `${name}.[name].chunk.js`,
      library: name,
      libraryTarget: "amd",
    },
  };
};

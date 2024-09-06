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
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");

const createIndexMiddleware = require("./middleware/IndexMiddleware");
const createContextPathMiddleware = require("./middleware/ContextPathMiddleware");

const isDevelopment = process.env.NODE_ENV === "development";
const root = path.resolve(__dirname, "..", "..");

const babelPlugins = [];
const webpackPlugins = [];

if (process.env.ANALYZE_BUNDLES === "true") {
  // it is ok to use require here, because we want to load the package conditionally
  // eslint-disable-next-line global-require
  const { BundleAnalyzerPlugin } = require("webpack-bundle-analyzer");
  webpackPlugins.push(new BundleAnalyzerPlugin());
}

let mode = "production";

if (isDevelopment) {
  mode = "development";
  babelPlugins.push(require.resolve("react-refresh/babel"));
  // it is ok to use require here, because we want to load the package conditionally
  // eslint-disable-next-line global-require
  const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin");
  webpackPlugins.push(new ReactRefreshWebpackPlugin());
}

const themedir = path.join(root, "ui-styles", "src");
const themes = fs
  .readdirSync(themedir)
  .map((filename) => path.parse(filename))
  .filter((p) => p.ext === ".scss")
  .reduce((entries, current) => ({ ...entries, [current.name]: path.join(themedir, current.base) }), {});

console.log(`build ${mode} bundles`);

const base = {
  mode,
  context: root,
  target: "web",
  resolveLoader: {
    modules: [path.join(__dirname, "..", "..", "..", "node_modules"), "node_modules"],
    extensions: [".js", ".json"],
    mainFields: ["loader", "main"],
  },
};

module.exports = [
  {
    ...base,
    entry: {
      webapp: [
        path.resolve(__dirname, "webpack-public-path.js"),
        // enable async/await
        "regenerator-runtime/runtime",
        "./ui-webapp/src/index.tsx",
      ],
    },
    devtool: "eval-cheap-module-source-map",
    module: {
      rules: [
        {
          test: /\.(mjs|js|ts|jsx|tsx)$/i,
          exclude: /node_modules/,
          use: [
            {
              loader: "babel-loader",
              options: {
                cacheDirectory: true,
                presets: ["@scm-manager/babel-preset"],
                plugins: babelPlugins,
              },
            },
          ],
        },
        {
          test: /\.(css|scss|sass)$/i,
          use: [
            // Creates `style` nodes from JS strings
            "style-loader",
            {
              loader: "css-loader",
              options: {
                // Run `postcss-loader` on each CSS `@import`, do not forget that `sass-loader` compile non CSS `@import`'s into a single file
                // If you need run `sass-loader` and `postcss-loader` on each CSS `@import` please set it to `2`
                importLoaders: 1,
                // Automatically enable css modules for files satisfying `/\.module\.\w+$/i` RegExp.
                modules: { auto: true },
              },
            },
            // Compiles Sass to CSS
            "sass-loader",
          ],
        },
        {
          test: /\.(png|svg|jpg|gif|woff2?|eot|ttf)$/,
          use: ["file-loader"],
        },
      ],
    },
    resolve: {
      extensions: [".ts", ".tsx", ".mjs", ".js", ".jsx", ".css", ".scss", ".json"],
      fallback: {
        fs: false,
        net: false,
        tls: false,
      },
      alias: {
        "decode-named-character-reference": require.resolve("decode-named-character-reference"),
        // force cjs instead of esm
        // https://github.com/tannerlinsley/react-query/issues/3513
        "react-query/devtools": require.resolve("react-query/devtools"),
      },
    },
    output: {
      path: path.join(root, "build", "webapp", "assets"),
      filename: "[name].bundle.js",
      chunkFilename: "[name].bundle.js",
    },
    devServer: {
      static: [
        {
          directory: path.join(root, "ui-webapp", "public"),
        },
      ],
      client: {
        overlay: {
          errors: true,
          warnings: false,
        },
      },
      historyApiFallback: true,
      host: "127.0.0.1",
      port: 3000,
      hot: true,
      devMiddleware: {
        index: false,
        publicPath: "/assets/",
      },
      onBeforeSetupMiddleware: ({ app }) => {
        app.use(createContextPathMiddleware("/scm"));
      },
      onAfterSetupMiddleware: ({ app }) => {
        const templatePath = path.join(root, "ui-webapp", "public", "index.mustache");
        const stage = process.env.NODE_ENV || "DEVELOPMENT";
        const renderParams = {
          contextPath: "/scm",
          scmStage: stage.toUpperCase(),
        };
        app.use(createIndexMiddleware(templatePath, renderParams));
      },
    },
    optimization: {
      runtimeChunk: "single",
      chunkIds: "named",
      splitChunks: {
        chunks: "initial",
        cacheGroups: {
          defaultVendors: {
            test: /[\\/]node_modules[\\/]/,
            priority: -10,
            filename: "vendors~webapp.bundle.js",
            reuseExistingChunk: true,
          },
          default: {
            minChunks: 2,
            priority: -20,
            reuseExistingChunk: true,
          },
        },
      },
    },
    plugins: webpackPlugins,
  },
  {
    ...base,
    entry: themes,
    module: {
      rules: [
        {
          test: /\.(ttf|eot|svg|png|jpg|gif|ico)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
          use: [
            {
              loader: "file-loader",
            },
          ],
        },
        {
          test: /\.(woff(2)?|ttf|eot|svg)(\?v=\d+\.\d+\.\d+)?$/,
          use: [
            {
              loader: "file-loader",
            },
          ],
        },
        {
          test: /\.(css|scss|sass)$/i,
          use: [
            {
              loader: MiniCssExtractPlugin.loader,
            },
            "css-loader",
            "sass-loader",
          ],
        },
      ],
    },
    plugins: [
      new MiniCssExtractPlugin({
        filename: "ui-theme-[name].css",
        ignoreOrder: false,
      }),
    ],
    optimization: {
      // TODO only on production?
      minimizer: [new OptimizeCSSAssetsPlugin({})],
    },
    output: {
      path: path.join(root, "build", "webapp", "assets"),
      filename: "ui-theme-[name].bundle.js",
    },
  },
];

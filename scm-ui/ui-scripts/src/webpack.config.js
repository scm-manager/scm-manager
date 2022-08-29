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
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");

const createIndexMiddleware = require("./middleware/IndexMiddleware");
const createContextPathMiddleware = require("./middleware/ContextPathMiddleware");

const isDevelopment = process.env.NODE_ENV === "development";
const root = path.resolve(process.cwd(), "..");

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
    modules: [path.join(__dirname, "..", "node_modules"), "node_modules"],
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
        "./ui-webapp/src/index.tsx"
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
  {
    ...base,
    entry: {
      polyfills: "./ui-polyfill/src/index.js",
    },
    output: {
      path: path.resolve(root, "build", "webapp", "assets"),
      filename: "[name].bundle.js",
    },
  },
];

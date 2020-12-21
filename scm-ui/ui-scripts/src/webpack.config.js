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
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");
const WorkerPlugin = require("worker-plugin");

const createIndexMiddleware = require("./middleware/IndexMiddleware");
const createContextPathMiddleware = require("./middleware/ContextPathMiddleware");

const isDevelopment = process.env.NODE_ENV === "development";
const root = path.resolve(process.cwd(), "scm-ui");

const babelPlugins = [];
const webpackPlugins = [new WorkerPlugin()];

let mode = "production";

if (isDevelopment) {
  mode = "development";
  babelPlugins.push(require.resolve("react-refresh/babel"));
  // it is ok to use require here, because we want to load the package conditionally
  // eslint-disable-next-line global-require
  const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin");
  webpackPlugins.push(new ReactRefreshWebpackPlugin());
}

console.log(`build ${mode} bundles`);

module.exports = [
  {
    mode,
    stats: "minimal",
    context: root,
    entry: {
      webapp: [path.resolve(__dirname, "webpack-public-path.js"), "./ui-webapp/src/index.tsx"]
    },
    devtool: "cheap-module-eval-source-map",
    target: "web",
    node: {
      fs: "empty",
      net: "empty",
      tls: "empty"
    },
    module: {
      rules: [
        {
          parser: {
            system: false,
            systemjs: false
          }
        },
        {
          test: /\.(js|ts|jsx|tsx)$/i,
          exclude: /node_modules/,
          use: [
            {
              loader: "babel-loader",
              options: {
                cacheDirectory: true,
                presets: ["@scm-manager/babel-preset"],
                plugins: babelPlugins
              }
            }
          ]
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
                modules: { auto: true }
              }
            },
            // Compiles Sass to CSS
            "sass-loader"
          ]
        },
        {
          test: /\.(png|svg|jpg|gif|woff2?|eot|ttf)$/,
          use: ["file-loader"]
        }
      ]
    },
    resolve: {
      extensions: [".ts", ".tsx", ".js", ".jsx", ".css", ".scss", ".json"]
    },
    output: {
      path: path.join(root, "build", "assets"),
      filename: "[name].bundle.js",
      chunkFilename: "[name].bundle.js"
    },
    devServer: {
      contentBase: path.join(root, "ui-webapp", "public"),
      compress: false,
      historyApiFallback: true,
      overlay: true,
      port: 3000,
      before: app => {
        app.use(createContextPathMiddleware("/scm"));
      },
      after: app => {
        const templatePath = path.join(root, "ui-webapp", "public", "index.mustache");
        const stage = process.env.NODE_ENV || "DEVELOPMENT";
        const renderParams = {
          contextPath: "/scm",
          scmStage: stage.toUpperCase()
        };
        app.use(createIndexMiddleware(templatePath, renderParams));
      },
      publicPath: "/assets/"
    },
    optimization: {
      runtimeChunk: "single",
      namedChunks: true,
      splitChunks: {
        chunks: "all",
        cacheGroups: {
          vendors: {
            test: /[\\/]node_modules[\\/]/,
            priority: -10
            // chunks: chunk => chunk.name !== "polyfill"
          },
          default: {
            minChunks: 2,
            priority: -20,
            reuseExistingChunk: true
          }
        }
      }
    },
    plugins: webpackPlugins
  },
  {
    context: root,
    entry: "./ui-styles/src/scm.scss",
    module: {
      rules: [
        {
          test: /\.(css|scss|sass)$/i,
          use: [
            {
              loader: MiniCssExtractPlugin.loader
            },
            "css-loader",
            "sass-loader"
          ]
        },
        {
          test: /\.(png|svg|jpg|gif|woff2?|eot|ttf)$/,
          use: ["file-loader"]
        }
      ]
    },
    plugins: [
      new MiniCssExtractPlugin({
        filename: "ui-styles.css",
        ignoreOrder: false
      })
    ],
    optimization: {
      minimizer: [new OptimizeCSSAssetsPlugin({})]
    },
    output: {
      path: path.join(root, "build", "assets"),
      filename: "ui-styles.bundle.js"
    }
  },
  {
    context: path.resolve(root),
    entry: {
      polyfills: "./ui-polyfill/src/index.js"
    },
    output: {
      path: path.resolve(root, "build", "assets"),
      filename: "[name].bundle.js"
    }
  }
];

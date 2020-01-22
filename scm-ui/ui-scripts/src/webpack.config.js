const path = require("path");
const createIndexMiddleware = require("./middleware/IndexMiddleware");
const createContextPathMiddleware = require("./middleware/ContextPathMiddleware");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");
const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin");

const isDevelopment = process.env.NODE_ENV === "development";
const root = path.resolve(process.cwd(), "scm-ui");

module.exports = [
  {
    mode: isDevelopment ? "development" : "production",
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
          test: /\.worker\.(j|t)s$/,
          use: { loader: "worker-loader" }
        },
        {
          test: /\.(js|ts|jsx|tsx)$/i,
          exclude: /node_modules/,
          use: [
            {
              loader: "cache-loader"
            },
            {
              loader: "thread-loader"
            },
            {
              loader: "babel-loader",
              options: {
                cacheDirectory: true,
                presets: ["@scm-manager/babel-preset"],
                plugins: [isDevelopment && require.resolve("react-refresh/babel")].filter(Boolean)
              }
            }
          ]
        },
        {
          test: /\.(css|scss|sass)$/i,
          use: [
            // Creates `style` nodes from JS strings
            "style-loader",
            // Translates CSS into CommonJS
            "css-loader",
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
      path: path.join(root, "target", "assets"),
      filename: "[name].bundle.js"
    },
    devServer: {
      contentBase: path.join(root, "ui-webapp", "public"),
      compress: false,
      historyApiFallback: true,
      overlay: true,
      port: 3000,
      before: function(app) {
        app.use(createContextPathMiddleware("/scm"));
      },
      after: function(app) {
        const templatePath = path.join(root, "ui-webapp", "public", "index.mustache");
        const renderParams = {
          contextPath: "/scm"
        };
        app.use(createIndexMiddleware(templatePath, renderParams));
      },
      publicPath: "/assets/"
    },
    optimization: {
      runtimeChunk: "single",
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
    plugins: [isDevelopment && new ReactRefreshWebpackPlugin()].filter(Boolean)
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
      path: path.join(root, "target", "assets"),
      filename: "ui-styles.bundle.js"
    }
  },
  {
    context: path.resolve(root),
    entry: {
      polyfills: "./ui-polyfill/src/index.js"
    },
    output: {
      path: path.resolve(root, "target", "assets"),
      filename: "[name].bundle.js"
    }
  }
];

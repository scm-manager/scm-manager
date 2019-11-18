const path = require("path");
const fs = require("fs");

const root = process.cwd();

const packageJsonPath = path.join(root, "package.json");
const packageJSON = JSON.parse(fs.readFileSync(packageJsonPath, { encoding: "UTF-8" }));

let name = packageJSON.name;
const orgaIndex = name.indexOf("/");
if (orgaIndex > 0) {
  name = name.substring(orgaIndex + 1);
}

module.exports = function(mode) {
  return {
    context: root,
    entry: {
      [name]: [path.resolve(__dirname, "webpack-public-path.js"), packageJSON.main || "src/main/js/index.js"]
    },
    mode,
    devtool: "source-map",
    target: "web",
    node: {
      fs: "empty",
      net: "empty",
      tls: "empty"
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
      "react-redux"
    ],
    module: {
      rules: [
        {
          test: /\.(js|ts|jsx|tsx)$/i,
          exclude: /node_modules/,
          use: {
            loader: "babel-loader",
            options: {
              presets: ["@scm-manager/babel-preset"]
            }
          }
        },
        {
          test: /\.(css|scss|sass)$/i,
          use: ["style-loader", "css-loader", "sass-loader"]
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
      path: path.join(root, "target", name + "-" + packageJSON.version, "webapp", "assets"),
      filename: "[name].bundle.js",
      chunkFilename: name + ".[name].chunk.js",
      library: name,
      libraryTarget: "amd"
    }
  };
};

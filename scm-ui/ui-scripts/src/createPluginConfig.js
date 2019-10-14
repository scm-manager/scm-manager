const path = require("path");
const fs = require("fs");

const root = process.cwd();

const packageJsonPath = path.join(root, "package.json");
const packageJSON = JSON.parse(
  fs.readFileSync(packageJsonPath, { encoding: "UTF-8" })
);

let name = packageJSON.name;
const orgaIndex = name.indexOf("/");
if (orgaIndex > 0) {
  name = name.substring(orgaIndex + 1);
}

module.exports = function(mode) {
  return {
    context: root,
    entry: {
      [name]: "./src/main/js/index.js"
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
      "@scm-manager/ui-components"
    ],
    module: {
      rules: [
        {
          test: /\.(js|jsx)$/,
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
    output: {
      path: path.join(
        root,
        "target",
        name + "-" + packageJSON.version,
        "webapp",
        "assets"
      ),
      filename: "[name].bundle.js",
      library: name,
      libraryTarget: "amd"
    }
  };
};

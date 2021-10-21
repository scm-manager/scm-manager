// .storybook/main.js

const path = require("path");
const fs = require("fs");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

const root = path.resolve("..");

const themedir = path.join(root, "ui-styles", "src");

const themes = fs
  .readdirSync(themedir)
  .map((filename) => path.parse(filename))
  .filter((p) => p.ext === ".scss")
  .reduce((entries, current) => ({ ...entries, [current.name]: path.join(themedir, current.base) }), {});

module.exports = {
  stories: ["../src/**/*.stories.tsx"],
  addons: ["storybook-addon-i18next", "storybook-addon-themes"],
  webpackFinal: async (config) => {
    config.entry = {
      main: config.entry,
      ...themes,
    };

    config.module.rules.push({
      test: /\.scss$/,
      use: [
        "css-loader",
        "sass-loader",
      ],
    });

    config.plugins.push(
      new MiniCssExtractPlugin({
        filename: "ui-theme-[name].css",
        ignoreOrder: false,
      })
    );

    return config;
  },
};

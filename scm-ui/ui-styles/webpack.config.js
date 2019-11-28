const path = require("path");

module.exports = {
  entry: "./src/scm.scss",
  devtool: "cheap-module-eval-source-map",
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
          "sass-loader"
        ]
      },
      {
        test: /\.(png|svg|jpg|gif|woff2?|eot|ttf)$/,
        use: ["file-loader"]
      }
    ]
  },
  output: {
    filename: "ui-styles.bundle.js"
  },
  devServer: {
    contentBase: path.join(__dirname, "public"),
    compress: false,
    overlay: true,
    port: 5000,
  }
};

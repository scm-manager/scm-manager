const path = require("path");

module.exports = {
  context: path.resolve(__dirname, ".."),
  entry: {
    webapp: "./ui-webapp/src/index.js"
  },
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
        test: /\.(js|jsx)$/,
        // exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: [
              "@babel/preset-env",
              "@babel/preset-react",
              "@babel/preset-flow"
            ],
            plugins: ["@babel/plugin-proposal-class-properties"]
          }
        }
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
  output: {
    path: path.resolve(__dirname, "..", "target"),
    filename: "[name].bundle.js"
  }
};

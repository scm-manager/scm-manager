module.exports = () => ({
  presets: [
    require("@babel/preset-env"),
    require("@babel/preset-flow"),
    require("@babel/preset-react")
  ],
  plugins: [
    require("@babel/plugin-proposal-class-properties"),
    require("@babel/plugin-proposal-optional-chaining")
  ]
});

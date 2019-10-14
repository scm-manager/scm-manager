const webpack = require("webpack");
const createPluginConfig = require("../createPluginConfig");

const config = createPluginConfig("development");
const compiler = webpack(config);

compiler.watch({}, (err, stats) => {
  console.log(stats.toString({
    colors: true
  }));
});

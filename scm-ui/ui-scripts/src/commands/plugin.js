const webpack = require("webpack");
const createPluginConfig = require("../createPluginConfig");

const config = createPluginConfig("production");

webpack(config, (err, stats) => {
  console.log(stats.toString({
    colors: true
  }));
  if (err || stats.hasErrors()) {
    process.exit(1);
  }
});

const babelJest = require("babel-jest");
const transformer = babelJest.createTransformer({
  presets: ["@scm-manager/babel-preset"],
  plugins: ["require-context-hook"],
  babelrc: false,
  configFile: false
});

module.exports = {
  ...transformer,
  process(src, filename) {
    if (
      !filename.includes("node_modules") ||
      filename.includes("@scm-manager")
    ) {
      return transformer.process(...arguments);
    }
    return src;
  }
};

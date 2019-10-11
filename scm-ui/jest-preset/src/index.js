const { createTransformer } = require("babel-jest");
const transformer = createTransformer({
  presets: ["@scm-manager/babel-preset"],
  plugins: ["require-context-hook"]
});

module.exports = {
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

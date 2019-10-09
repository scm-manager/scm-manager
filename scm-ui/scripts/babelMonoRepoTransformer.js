/**
 * Read and use .babelrc from packages
 */
const { join, resolve } = require("path");
const { createTransformer } = require("babel-jest");

const packagePath = resolve(__dirname, "../");
const packageGlob = join(packagePath, "*");

module.exports = createTransformer({
  babelrcRoots: packageGlob
});

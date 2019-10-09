const path = require("path");
const fs = require("fs");
const transformer = path.resolve(__dirname, "babelPluginTransformer.js");
const rootDir = path.resolve(process.cwd());
const packageJsonPath = path.join(rootDir, "package.json");
const packageJson = JSON.parse(
  fs.readFileSync(packageJsonPath, { encoding: "UTF-8" })
);

const reportDirectory = path.join(rootDir, "target", "jest-reports");

module.exports = {
  rootDir,
  transform: { "^.+\\.js$": transformer },
  transformIgnorePatterns: [".*/node_modules/.*"],
  collectCoverage: true,
  coverageDirectory: path.join(reportDirectory, "coverage"),
  coveragePathIgnorePatterns: ["src/main/js/tests/.*"],
  reporters: [
    "default",
    [
      "jest-junit",
      {
        suiteName: packageJson.name + " tests",
        outputDirectory: reportDirectory,
        outputName: "TEST-plugin.xml"
      }
    ]
  ]
};

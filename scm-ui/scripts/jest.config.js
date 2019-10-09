const path = require("path");
const rootDir = path.resolve(__dirname, "..");
const reportDirectory = path.join(rootDir, "target", "jest-reports");

module.exports = {
  rootDir,
  transform: { "^.+\\.js$": "./scripts/babelMonoRepoTransformer.js" },
  collectCoverage: true,
  coverageDirectory: path.join(reportDirectory, "coverage"),
  coveragePathIgnorePatterns: ["src/tests/.*"],
  reporters: [
    "default",
    [
      "jest-junit",
      { outputDirectory: reportDirectory, outputName: "TEST-all.xml" }
    ]
  ]
};

const path = require("path");
const rootDir = path.resolve(__dirname, "..");
const reportDirectory = path.join(rootDir, "target", "jest-reports");

module.exports = {
  rootDir,
  transform: { "^.+\\.js$": "@scm-manager/jest-preset" },
  collectCoverage: true,
  coverageDirectory: path.join(reportDirectory, "coverage"),
  coveragePathIgnorePatterns: ["src/tests/.*"],
  reporters: [
    "default",
    [
      "jest-junit",
      {
        suiteName: "SCM-UI Package tests",
        outputDirectory: reportDirectory,
        outputName: "TEST-scm-ui.xml"
      }
    ]
  ]
};

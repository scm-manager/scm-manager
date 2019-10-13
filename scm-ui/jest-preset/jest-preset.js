const path = require("path");
const rootDir = path.resolve(__dirname, "..");
const reportDirectory = path.join(rootDir, "target", "jest-reports");
const mockDirectory = path.resolve(__dirname, "src", "__mocks__");

// Set timezone for tests, this is required to get same date values
// accross diferent machines such ci-server and dev box.
// We have to set the timezone as soon as possible, because Date will
// cache the value.
// @see https://stackoverflow.com/questions/56261381/how-do-i-set-a-timezone-in-my-jest-config
process.env.TZ = "Europe/Berlin";

module.exports = {
  rootDir,
  transform: {
    "^.+\\.js$": "@scm-manager/jest-preset"
  },
  moduleNameMapper: {
    "\\.(png|svg|jpg|gif|woff2?|eot|ttf)$": path.join(
      mockDirectory,
      "fileMock.js"
    ),
    "\\.(css|scss|sass)$": path.join(mockDirectory, "styleMock.js")
  },
  setupFiles: [path.resolve(__dirname, "src", "setup.js")],
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

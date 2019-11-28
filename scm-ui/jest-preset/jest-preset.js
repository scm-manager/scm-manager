const path = require("path");
const mockDirectory = path.resolve(__dirname, "src", "__mocks__");
const findName = require("./src/findName");
const findTarget = require("./src/findTarget");

// Set timezone for tests, this is required to get same date values
// accross diferent machines such ci-server and dev box.
// We have to set the timezone as soon as possible, because Date will
// cache the value.
// @see https://stackoverflow.com/questions/56261381/how-do-i-set-a-timezone-in-my-jest-config
process.env.TZ = "Europe/Berlin";

const root = process.cwd();
const name = findName(root);
const target = findTarget(root);
const reportDirectory = path.join(target, "jest-reports");

module.exports = {
  rootDir: root,
  roots: [
    root
  ],
  testPathDirs: [
    path.join(root, "src")
  ],
  transform: {
    "^.+\\.(ts|tsx|js)$": "@scm-manager/jest-preset"
  },
  transformIgnorePatterns: [
    "node_modules/(?!(@scm-manager)/)"
  ],
  moduleNameMapper: {
    "\\.(png|svg|jpg|gif|woff2?|eot|ttf)$": path.join(
      mockDirectory,
      "fileMock.js"
    ),
    "\\.(css|scss|sass)$": path.join(mockDirectory, "styleMock.js")
  },
  setupFiles: [path.resolve(__dirname, "src", "setup.js")],
  collectCoverage: true,
  collectCoverageFrom: [
    "src/**/*.{ts,tsx,js,jsx}"
  ],
  coverageDirectory: path.join(reportDirectory, "coverage-" + name),
  coveragePathIgnorePatterns: [
    "src/tests/.*",
    "src/testing/.*"
  ],
  reporters: [
    "default",
    [
      "jest-junit",
      {
        suiteName: name + " tests",
        outputDirectory: reportDirectory,
        outputName: "TEST-" + name + ".xml"
      }
    ]
  ]
};

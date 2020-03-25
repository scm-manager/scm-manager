/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
  roots: [root],
  testPathDirs: [path.join(root, "src")],
  transform: {
    "^.+\\.(ts|tsx|js)$": "@scm-manager/jest-preset"
  },
  transformIgnorePatterns: ["node_modules/(?!(@scm-manager)/)"],
  moduleNameMapper: {
    "\\.(png|svg|jpg|gif|woff2?|eot|ttf)$": path.join(mockDirectory, "fileMock.js"),
    "\\.(css|scss|sass)$": path.join(mockDirectory, "styleMock.js")
  },
  setupFiles: [path.resolve(__dirname, "src", "setup.js")],
  collectCoverage: true,
  collectCoverageFrom: ["src/**/*.{ts,tsx,js,jsx}"],
  coverageDirectory: path.join(reportDirectory, "coverage-" + name),
  coveragePathIgnorePatterns: ["src/tests/.*", "src/testing/.*"],
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

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
const { spawnSync } = require("child_process");
const os = require("os");

const yarnCmd = os.platform() === "win32" ? "yarn.cmd" : "yarn";

const yarn = (args) => {
  const result = spawnSync(yarnCmd, args, { stdio: "inherit" });
  if (result.error) {
    console.log("could not start yarn command:", result.error);
    process.exit(2);
  } else if (result.status !== 0) {
    console.log("yarn process ends with status code:", result.status);
    process.exit(3);
  }
};

const version = (v) => {
  yarn(["version", "--no-git-tag-version", "--new-version", v]);
};

const publish = (v) => {
  yarn(["publish", "--new-version", v]);
};

module.exports = {
  version,
  publish,
  yarn,
};

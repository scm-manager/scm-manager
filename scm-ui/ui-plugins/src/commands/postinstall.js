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
/* eslint-disable no-console */
const path = require("path");
const fs = require("fs");
const { spawnSync } = require("child_process");

const packageJsonPath = path.join(process.cwd(), "package.json");
const packageJSON = JSON.parse(fs.readFileSync(packageJsonPath, "UTF-8"));

const reference = require("../../package.json");

const sync = (left, right, key) => {
  if (!right[key]) {
    right[key] = {};
  }

  let changed = false;

  const keys = Object.keys(left[key]);
  keys.forEach((name) => {
    if (right[key][name] !== left[key][name]) {
      console.log(name, "has changed from", right[key][name], "to", left[key][name]);
      right[key][name] = left[key][name];
      changed = true;
    }
  });

  return changed;
};

const update = () => {
  const dep = sync(reference, packageJSON, "dependencies");
  const devDep = sync(reference, packageJSON, "devDependencies");
  return dep || devDep;
};

if (update()) {
  console.log("dependencies changed, install new dependencies");

  fs.writeFileSync(packageJsonPath, JSON.stringify(packageJSON, null, "  "), { encoding: "UTF-8" });

  const result = spawnSync("yarn", ["install"], { stdio: "inherit" });
  if (result.error) {
    console.log("could not start yarn command:", result.error);
    process.exit(2);
  } else if (result.status !== 0) {
    console.log("yarn process ends with status code:", result.status);
    process.exit(3);
  }
}

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

const fs = require("fs");
const { promisify } = require("util");

const writeFile = promisify(fs.writeFile);

const writeJson = async (path, content) => {
  await writeFile(path, JSON.stringify(content, null, 2), { encoding: "utf8" })
};

const updateDependencies = (dependencies, packages, newVersion) => {
  if (!dependencies) {
    return;
  }
  Object.keys(dependencies).forEach(name => {
    if (packages.includes(name)) {
      dependencies[name] = newVersion;
    }
  });
};

const setVersion = async (workspaces, newVersion) => {
  const packages = workspaces.map(workspace => workspace.package.name);

  for (const workspace of workspaces) {
    workspace.package.version = newVersion;
    updateDependencies(workspace.package.dependencies, packages, newVersion);
    updateDependencies(workspace.package.devDependencies, packages, newVersion);
    updateDependencies(workspace.package.optionalDependencies, packages, newVersion);
    updateDependencies(workspace.package.peerDependencies, packages, newVersion);
    await writeJson(workspace.packageJsonPath, workspace.package);
  }

  return packages;
};

module.exports = setVersion;

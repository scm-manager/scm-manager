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
const resolveWorkspaces = require("../resolveWorkspaces");
const fs = require("fs");
const { promisify } = require("util");

const writeFile = promisify(fs.writeFile);

const args = process.argv.slice(2);

if (args.length < 1) {
  console.log("usage ui-scripts version <new-version>");
  process.exit(1);
}

const newVersion = args[0];

const writeJson = async (path, content) => {
  await writeFile(path, JSON.stringify(2, null, content), { encoding: "utf8" })
};

const updateDependencies = (dependencies, packages) => {
  if (!dependencies) {
    return;
  }
  Object.keys(dependencies).forEach(name => {
    if (packages.includes(name)) {
      dependencies[name] = newVersion;
    }
  });
};

const setVersions = async () => {
  const workspaces = await resolveWorkspaces();
  const packages = workspaces.map(workspace => workspace.package.name);

  for (const workspace of workspaces) {
    workspace.package.version = newVersion;
    updateDependencies(workspace.package.dependencies, packages);
    updateDependencies(workspace.package.devDependencies, packages);
    updateDependencies(workspace.package.optionalDependencies, packages,);
    updateDependencies(workspace.package.peerDependencies, packages);
    await writeJson(workspace.path, workspace.package);
  }

  return packages;
};

setVersions()
  .then()
  .catch(err => {
    console.error("failed to update versions:", err);
    process.exit(1);
  });

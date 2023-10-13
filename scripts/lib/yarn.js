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
const path = require("path");
const fs = require("fs");

const yarnCmd = os.platform() === "win32" ? "yarn.cmd" : "yarn";

const yarn = (args, cwd) => {
  const result = spawnSync(yarnCmd, args, { stdio: "inherit", cwd });
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

const findWorkspaces = () => {
  const result = spawnSync(yarnCmd, ["workspaces", "info"]);
  return JSON.parse(result.stdout);
};

const updateDependencies = (workspaces, dependencies, v) => {
  if (dependencies) {
    Object.keys(dependencies).forEach((dep) => {
      if (workspaces[dep]) {
        dependencies[dep] = v;
      }
    });
  }
};

const workspaceVersion = (v) => {
  const workspaces = findWorkspaces();
  Object.keys(workspaces).forEach((name) => {
    const workspace = workspaces[name];
    const packageJsonPath = path.join(process.cwd(), workspace.location, "package.json");

    const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, { encoding: "utf8" }));
    packageJson.version = v;

    updateDependencies(workspaces, packageJson.dependencies, v);
    updateDependencies(workspaces, packageJson.devDependencies, v);
    updateDependencies(workspaces, packageJson.peerDependencies, v);

    fs.writeFileSync(packageJsonPath, JSON.stringify(packageJson, null, 2), { encoding: "utf8" });
  });
};

const forEachModule = (fn) => {
  const workspaces = findWorkspaces();
  Object.keys(workspaces).forEach((name) => {
    const workspace = workspaces[name];
    const cwd = path.join(process.cwd(), workspace.location);
    const packageJson = JSON.parse(fs.readFileSync(path.join(cwd, "package.json"), { encoding: "utf8" }));
    fn(packageJson, cwd);
  });
};

const workspacePublish = (v) => {
  forEachModule((module, cwd) => {
    if (!module.private) {
      console.log(`publish module ${module.name}`);
      yarn(["publish", "--new-version", v], cwd);
    } else {
      console.log(`skip private module ${module.name}`);
    }
  });
};

module.exports = {
  version,
  publish,
  yarn,
  workspaceVersion,
  workspacePublish,
};

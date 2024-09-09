/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

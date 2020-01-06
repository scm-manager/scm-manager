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
  keys.forEach(name => {
    if (right[key][name] !== left[key][name]) {
      console.log(name, "has changed from", right[key][name], "to", left[key][name]);
      right[key][name] = left[key][name];
      changed = true;
    }
  });

  return changed;
};

const update = () => {
  let dep = sync(reference, packageJSON, "dependencies");
  let devDep = sync(reference, packageJSON, "devDependencies");
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

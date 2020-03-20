const { spawnSync } = require("child_process");
const os = require("os");

const yarnCmd = os.platform() === "win32" ? "yarn.cmd" : "yarn";

const yarn = args => {
  const result = spawnSync(yarnCmd, args, { stdio: "inherit" });
  if (result.error) {
    console.log("could not start yarn command:", result.error);
    process.exit(2);
  } else if (result.status !== 0) {
    console.log("yarn process ends with status code:", result.status);
    process.exit(3);
  }
};

const version = version => {
  yarn([
    "run",
    "lerna",
    "--no-git-tag-version",
    "--no-push",
    "version",
    "--yes",
    version
  ]);
};

const publish = () => {
  yarn(["run", "lerna", "publish", "from-package", "--yes"]);
};

module.exports = {
  version,
  publish
};

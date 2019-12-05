#!/usr/bin/env node
const { spawnSync } = require("child_process");

const commands = ["plugin", "plugin-watch", "publish", "version"];

const args = process.argv.slice(2);

const commandIndex = args.findIndex(arg => {
  return commands.includes(arg);
});

const command = commandIndex === -1 ? args[0] : args[commandIndex];
const nodeArgs = commandIndex > 0 ? args.slice(0, commandIndex) : [];

if (commands.includes(command)) {
  const result = spawnSync(
    "node",
    nodeArgs
      .concat(require.resolve("../src/commands/" + command))
      .concat(args.slice(commandIndex + 1)),
    { stdio: "inherit" }
  );
  if (result.signal) {
    if (result.signal === "SIGKILL") {
      console.log(
        "The build failed because the process exited too early. " +
        "This probably means the system ran out of memory or someone called " +
        "`kill -9` on the process."
      );
    } else if (result.signal === "SIGTERM") {
      console.log(
        "The build failed because the process exited too early. " +
        "Someone might have called `kill` or `killall`, or the system could " +
        "be shutting down."
      );
    }
    process.exit(1);
  }
  process.exit(result.status);
} else {
  console.log("Unknown script \"" + command + "\".");
  console.log("Perhaps you need to update react-scripts?");
  console.log(
    "See: https://facebook.github.io/create-react-app/docs/updating-to-new-releases"
  );
}

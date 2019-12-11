const lerna = require("../lerna");

const args = process.argv.slice(2);

if (args.length < 1) {
  console.log("usage ui-scripts version <new-version>");
  process.exit(1);
}

const version = args[0];

lerna.version(version);

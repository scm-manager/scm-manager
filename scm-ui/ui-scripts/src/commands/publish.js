const lerna = require("../lerna");
const versions = require("../versions");

const args = process.argv.slice(2);

if (args.length < 1) {
  console.log("usage ui-scripts publish version");
  process.exit(1);
}

const version = args[0];
const index = version.indexOf("-SNAPSHOT");
if (index > 0) {
  const snapshotVersion = version.substring(0, index)  + "-" + versions.createSnapshotVersion();
  console.log("publish snapshot release " + snapshotVersion);
  lerna.version(snapshotVersion);
  lerna.publish();
  lerna.version(version);
} else {
  // ?? not sure
  lerna.publish();
}





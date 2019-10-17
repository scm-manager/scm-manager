const fs = require("fs");
const path = require("path");

function findMavenModuleRoot(directory) {
  if (fs.existsSync(path.join(directory, "pom.xml"))) {
    return directory;
  }
  return findMavenModuleRoot(path.resolve(directory, ".."));
}

function findTarget(directory) {
  const moduleRoot = findMavenModuleRoot(directory);
  return path.join(moduleRoot, "target");
}

module.exports = findTarget;

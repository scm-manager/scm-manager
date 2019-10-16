const path = require("path");
const fs = require("fs");

function findName(directory) {
  const packageJSON = JSON.parse(fs.readFileSync(path.join(directory, "package.json"), {encoding: "UTF-8"}));

  let name = packageJSON.name;
  const orgaIndex = name.indexOf("/");
  if (orgaIndex > 0) {
    return name.substring(orgaIndex + 1);
  }
  return name;
}

module.exports = findName;

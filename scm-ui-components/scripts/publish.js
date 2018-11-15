const fs = require("fs");
const path = require("path");
const {spawn} = require("child_process");
const parseString = require('xml2js').parseString;

function isSnapshot(version) {
  return version.indexOf("SNAPSHOT") > 0;
}

function createSnapshotVersion(version) {
  const date = new Date();
  const year = date.getFullYear();
  const month = date.getMonth().toString().padStart(2, "0");
  const day = date.getDate().toString().padStart(2, "0");

  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  const seconds = date.getSeconds().toString().padStart(2, "0");

  return version.replace("SNAPSHOT", `${year}${month}${day}-${hours}${minutes}${seconds}`);
}

function createVersionForPublishing(version) {
  if (isSnapshot(version)) {
    return createSnapshotVersion(version);
  }
  return version;
}

function publishPackages(version) {
  console.log(`publish ${version} of all packages`);
  return new Promise((resolve, reject) => {
    const lerna = spawn("lerna", [
      "exec", "--", "yarn", "publish", "--new-version", version, "--access", "public"
    ], {
      stdio: [
        process.stdin,
        process.stdout,
        process.stderr
      ]
    });

    lerna.on('close', (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error("publishing of packages failed with status code: " + code));
      }
    });
  });
}

function setVersion(package, packages, newVersion) {
  return new Promise((resolve, reject) => {
    const packageJsonPath = path.join(package, "package.json");
    fs.readFile(packageJsonPath, "utf-8" , (err, content) => {
      if (err) {
        reject(err);
      } else {
        const packageJson = JSON.parse(content);
        packageJson.version = newVersion;

        for (let dep in packageJson.dependencies) {
          if (packages.indexOf(dep) >= 0) {
            packageJson.dependencies[ dep ] = newVersion;
          }
        }

        fs.writeFile( packageJsonPath, JSON.stringify(packageJson, null, 2), (err) => {
          if (err) {
            reject(err)
          } else {
            console.log("modified", packageJsonPath);
            resolve();
          }
        });
      }
    });
  });
}

function setVersions(newVersion) {
  console.log("set versions of packages to", newVersion);
  return new Promise((resolve, reject) => {
    fs.readdir("packages", (err, packages) => {
      if ( err ) {
        reject(err);
      } else {
        const actions = [];
        const packagesWithOrg = packages.map((name) => `@scm-manager/${name}`);
        for (let pkg of packages) {
          const action = setVersion(path.join("packages", pkg), packagesWithOrg, newVersion);
          actions.push(action);
        }
  
        resolve(Promise.all(actions));
      }
    });
  });
}

function getVersion() {
  return new Promise((resolve, reject) => {
    fs.readFile("pom.xml", "utf8", (err, xml) => {
      if (err) {
        reject(err);
      } else {
        
        parseString(xml, function (err, json) {
          if (err) {
            reject(err)
          } else {
            const project = json.project;
      
            let version = project.version;
            if (!version) {
              version = project.parent.version;
            }
            version = version[0];

            resolve(version)
          }
      
        });

      }
    });
  });
}

getVersion()
  .then(version => {
    const publishVersion = createVersionForPublishing(version);
    return setVersions(publishVersion)
      .then(() => publishPackages(publishVersion))
      .then(() => setVersions(version));
  })
  .catch((err) => {
    throw err;
  });

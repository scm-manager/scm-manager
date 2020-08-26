const cypress = require("cypress");
const fs = require("fs");
const path = require("path");

const options = {
  reporter: "junit",
  reporterOptions: {
    mochaFile: path.join("..", "target", "cypress-reports", "TEST-[hash].xml")
  }
};

cypress
  .run(options)
  .then(results => {
    results.runs.forEach(run => {
      // remove videos of successful runs
      if (!run.shouldUploadVideo) {
        fs.unlinkSync(run.video);
      }
    });
  })
  .catch(err => console.error(err));

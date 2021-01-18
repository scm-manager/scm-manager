/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

const cypress = require("cypress");
const fs = require("fs");
const path = require("path");
const ffmpegPath = require("@ffmpeg-installer/ffmpeg").path;
const ffmpeg = require("fluent-ffmpeg");

ffmpeg.setFfmpegPath(ffmpegPath);

const options = {
  reporter: "junit",
  reporterOptions: {
    mochaFile: path.join("..", "build", "reports", "e2e", "TEST-[hash].xml")
  }
};

const createOutputFile = test => {
  const title = test.title.join(" -- ");
  return path.join("cypress", "videos", `${title}.mp4`);
};

const cutVideo = (video, test) => {
  return new Promise((resolve, reject) => {
    const title = createOutputFile(test);
    ffmpeg(video)
      .setStartTime(test.videoTimestamp / 1000)
      .setDuration(test.wallClockDuration / 1000)
      .output(title)
      .on("end", err => {
        if (err) {
          reject(err);
        } else {
          resolve();
        }
      })
      .on("error", err => {
        reject(err);
      })
      .run();
  });
};

cypress
  .run(options)
  .then(results => {
    results.runs.forEach(run => {
      // remove videos of successful runs
      if (!run.shouldUploadVideo) {
        fs.unlinkSync(run.video);
      } else {
        const cuts = [];
        run.tests.forEach(test => {
          if (test.state !== "passed") {
            cuts.push(cutVideo(run.video, test));
          }
        });
        Promise.all(cuts)
          .then(() => fs.unlinkSync(run.video))
          .catch(err => console.error("failed to cut video", err));
      }
    });
  })
  .catch(err => console.error(err));

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
const fs = require("fs");
const util = require("util");
const glob = require("glob-promise");
const path = require("path");

const readFile = util.promisify(fs.readFile);

const readJson = async pathname => {
  const content = await readFile(pathname, { encoding: "utf8" });
  return JSON.parse(content);
};

const resolveWorkspaces = async () => {
  const rootPath = process.cwd();
  const packageJson = await readJson("./package.json");

  const entries = (await Promise.all(packageJson.workspaces.map(pattern => glob(pattern, { cwd: rootPath }))))
    .reduce((arr, filePaths) => {
      arr.push(...filePaths);
      return arr;
    }, [])
    .filter(p => !p.endsWith("target"))
    .filter(p => fs.statSync(p).isDirectory())
    .filter(p => fs.existsSync(path.join(p, "package.json")))
    .map(async p => {
      const pkg = await readJson(path.join(p, "package.json"));
      return {
        path: p,
        package: pkg
      };
    });

  return Promise.all(entries);
};

module.exports = resolveWorkspaces;

#!/usr/bin/env node
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
const path = require("path");

const commandDir = path.join(__dirname, "commands");
const commands = fs
  .readdirSync(commandDir)
  .map(script => {
    if (script.endsWith(".js")) {
      return script.replace(".js", "");
    }
    return undefined;
  })
  .filter(cmd => !!cmd);

const args = process.argv.slice(2);

const commandIndex = args.findIndex(arg => {
  return commands.includes(arg);
});

const commandName = commandIndex === -1 ? args[0] : args[commandIndex];
if (!commandName) {
  console.log(`Use plugin-scripts [${commands.join(", ")}]`);
} else if (commands.includes(commandName)) {
  // eslint-disable-next-line
  const command = require(path.join(commandDir, `${commandName}.js`));
  command(args.slice(commandIndex + 1));
} else {
  console.log(`Unknown script "${commandName}".`);
  console.log("Perhaps you need to update plugin-scripts?");
}

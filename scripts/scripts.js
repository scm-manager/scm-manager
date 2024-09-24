#!/usr/bin/env node
/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

const fs = require("fs");
const path = require("path");

const commandDir = path.join(__dirname, "commands");
const commands = fs
  .readdirSync(commandDir)
  .map((script) => {
    if (script.endsWith(".js")) {
      return script.replace(".js", "");
    }
    return undefined;
  })
  .filter((cmd) => !!cmd);

const args = process.argv.slice(2);

const commandIndex = args.findIndex((arg) => {
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

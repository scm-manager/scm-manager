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

// eslint-disable-next-line import/no-extraneous-dependencies
const { generateBinPath } = require("turbo/node-platform");
const { spawn } = require("child_process");

const turbo = spawn(generateBinPath(), process.argv.slice(2), { stdio: "inherit" });

turbo.on("close", (code) => {
  process.exit(code);
});

process.once("SIGTERM", () => {
  turbo.kill();
});

if (process.ppid) {
  const { ppid } = process;
  setInterval(() => {
    if (ppid !== process.ppid) {
      turbo.kill();
    }
  }, 500);
}

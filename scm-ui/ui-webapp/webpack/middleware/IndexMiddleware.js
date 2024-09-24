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

const mustache = require("mustache");
const fs = require("fs");
// disable escaping
mustache.escape = function(text) {
  return text;
};

function createIndexMiddleware(file, params) {
  const template = fs.readFileSync(file, { encoding: "UTF-8" });
  return function(req, resp, next) {
    if (req.url === "/index.html") {
      const content = mustache.render(template, params);
      resp.send(content);
    } else {
      next();
    }
  }
}

module.exports = createIndexMiddleware;

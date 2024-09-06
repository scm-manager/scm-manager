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

import parser from "ua-parser-js";

export default function splitKeyCombination(key: string, userAgent = window.navigator.userAgent) {
  const {
    os: { name: osName },
  } = parser(userAgent);
  const isMacOS = osName === "Mac OS";
  return key
    .replace(/(option|alt)/g, isMacOS ? "⌥" : "alt")
    .replace(/(command|meta)/g, isMacOS ? "⌘" : "meta")
    .replace("mod", isMacOS ? "⌘" : "ctrl")
    .replace(/shift/g, "⇧")
    .replace(/up/g, "↑")
    .replace(/down/g, "↓")
    .replace(/left/g, "←")
    .replace(/right/g, "→")
    .split(/[+ ]/);
}

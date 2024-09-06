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

import { BaseContext } from "./DiffTypes";
import { FileDiff, Hunk } from "@scm-manager/ui-types";

export function getPath(file: FileDiff) {
  if (file.type === "delete") {
    return file.oldPath;
  }
  return file.newPath;
}

export function createHunkIdentifier(file: FileDiff, hunk: Hunk) {
  const path = getPath(file);
  return `${file.type}_${path}_${hunk.content}`;
}

export function createHunkIdentifierFromContext(ctx: BaseContext) {
  return createHunkIdentifier(ctx.file, ctx.hunk);
}

export function escapeWhitespace(path: string) {
  return path?.toLowerCase().replace(/\W/g, "-");
}

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

export function getAnchorSelector(uriHashContent: string) {
  return "#" + escapeWhitespace(decodeURIComponent(uriHashContent));
}

export function getFileNameFromHash(hash: string) {
  const matcher = new RegExp(/^#diff-(.*)$/, "g");
  const match = matcher.exec(hash);
  return match ? match[1] : undefined;
}

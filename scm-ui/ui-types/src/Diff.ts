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

import { HalRepresentation, Links } from "./hal";

export type FileChangeType = "add" | "modify" | "delete" | "copy" | "rename";

export type Diff = HalRepresentation & {
  files: FileDiff[];
  partial: boolean;
  statistics?: Statistics;
};

export type FileDiff = {
  hunks?: Hunk[];
  newEndingNewLine: boolean;
  newMode?: string;
  newPath: string;
  newRevision?: string;
  oldEndingNewLine: boolean;
  oldMode?: string;
  oldPath: string;
  oldRevision?: string;
  type: FileChangeType;
  language?: string;
  syntaxModes?: { [mode: string]: string };
  // TODO does this property exists?
  isBinary?: boolean;
  _links?: Links;
};

export type Statistics = {
  added: number;
  deleted: number;
  modified: number;
}

export type Hunk = {
  changes: Change[];
  content: string;
  oldStart?: number;
  newStart?: number;
  oldLines?: number;
  newLines?: number;
  fullyExpanded?: boolean;
  expansion?: boolean;
};

export type ChangeType = "insert" | "delete" | "normal" | "conflict";

export type Change = {
  content: string;
  isNormal?: boolean;
  isInsert?: boolean;
  isDelete?: boolean;
  lineNumber?: number;
  newLineNumber?: number;
  oldLineNumber?: number;
  type: ChangeType;
};

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

import { apiClient } from "@scm-manager/ui-components";
import { Change, File, Hunk } from "./DiffTypes";
import { Link } from "@scm-manager/ui-types/src";

class DiffExpander {
  file: File;

  constructor(file: File) {
    this.file = file;
  }

  hunkCount = () => {
    if (this.file.hunks) {
      return this.file.hunks.length;
    } else {
      return 0;
    }
  };

  minLineNumber: (n: number) => number = (n: number) => {
    return this.file.hunks![n]!.newStart!;
  };

  maxLineNumber: (n: number) => number = (n: number) => {
    return this.file.hunks![n]!.newStart! + this.file.hunks![n]!.newLines! - 1;
  };

  computeMaxExpandHeadRange = (n: number) => {
    if (this.file.type === "delete") {
      return 0;
    } else if (n === 0) {
      return this.minLineNumber(n) - 1;
    }
    return this.minLineNumber(n) - this.maxLineNumber(n - 1) - 1;
  };

  computeMaxExpandBottomRange = (n: number) => {
    if (this.file.type === "add" || this.file.type === "delete") {
      return 0;
    }
    const changes = this.file.hunks![n].changes;
    if (changes[changes.length - 1].type === "normal") {
      if (n === this.file!.hunks!.length - 1) {
        return this.file!.hunks![this.file!.hunks!.length - 1].fullyExpanded ? 0 : -1;
      }
      return this.minLineNumber(n + 1) - this.maxLineNumber(n) - 1;
    } else {
      return 0;
    }
  };

  expandHead = (n: number, count: number, callback: (newFile: File) => void) => {
    const lineRequestUrl = (this.file._links!.lines as Link).href
      .replace("{start}", (this.minLineNumber(n) - Math.min(count, this.computeMaxExpandHeadRange(n)) - 1).toString())
      .replace("{end}", (this.minLineNumber(n) - 1).toString());
    apiClient
      .get(lineRequestUrl)
      .then(response => response.text())
      .then(text => text.split("\n"))
      .then(lines => this.expandHunkAtHead(n, lines, callback));
  };

  expandBottom = (n: number, count: number, callback: (newFile: File) => void) => {
    const maxExpandBottomRange = this.computeMaxExpandBottomRange(n);
    const lineRequestUrl = (this.file._links!.lines as Link).href
      .replace("{start}", this.maxLineNumber(n).toString())
      .replace(
        "{end}",
        count > 0
          ? (
              this.maxLineNumber(n) +
              Math.min(count, maxExpandBottomRange > 0 ? maxExpandBottomRange : Number.MAX_SAFE_INTEGER)
            ).toString()
          : "-1"
      );
    apiClient
      .get(lineRequestUrl)
      .then(response => response.text())
      .then(text => text.split("\n"))
      .then(lines => this.expandHunkAtBottom(n, count, lines, callback));
  };

  expandHunkAtHead = (n: number, lines: string[], callback: (newFile: File) => void) => {
    const hunk = this.file.hunks![n];
    if (lines[lines.length - 1] === "") {
      lines.pop();
    }
    const newChanges: Change[] = [];
    let oldLineNumber = hunk!.changes![0]!.oldLineNumber! - lines.length;
    let newLineNumber = hunk!.changes![0]!.newLineNumber! - lines.length;

    lines.forEach(line => {
      newChanges.push({
        content: line,
        type: "normal",
        oldLineNumber,
        newLineNumber,
        isNormal: true
      });
      oldLineNumber += 1;
      newLineNumber += 1;
    });
    hunk.changes.forEach(change => newChanges.push(change));

    const newHunk = {
      ...hunk,
      oldStart: hunk.oldStart! - lines.length,
      newStart: hunk.newStart! - lines.length,
      oldLines: hunk.oldLines! + lines.length,
      newLines: hunk.newLines! + lines.length,
      changes: newChanges
    };
    const newHunks: Hunk[] = [];
    this.file.hunks!.forEach((oldHunk: Hunk, i: number) => {
      if (i === n) {
        newHunks.push(newHunk);
      } else {
        newHunks.push(oldHunk);
      }
    });
    const newFile = { ...this.file, hunks: newHunks };
    callback(newFile);
  };

  expandHunkAtBottom = (n: number, requestedLines: number, lines: string[], callback: (newFile: File) => void) => {
    const hunk = this.file.hunks![n];
    if (lines[lines.length - 1] === "") {
      lines.pop();
    }
    const newChanges = [...hunk.changes];
    let oldLineNumber: number = this.getMaxOldLineNumber(newChanges);
    let newLineNumber: number = this.getMaxNewLineNumber(newChanges);

    lines.forEach(line => {
      oldLineNumber += 1;
      newLineNumber += 1;
      newChanges.push({
        content: line,
        type: "normal",
        oldLineNumber,
        newLineNumber,
        isNormal: true
      });
    });

    const newHunk = {
      ...hunk,
      oldLines: hunk.oldLines! + lines.length,
      newLines: hunk.newLines! + lines.length,
      changes: newChanges,
      fullyExpanded: requestedLines < 0 || lines.length < requestedLines
    };
    const newHunks: Hunk[] = [];
    this.file.hunks!.forEach((oldHunk: Hunk, i: number) => {
      if (i === n) {
        newHunks.push(newHunk);
      } else {
        newHunks.push(oldHunk);
      }
    });
    const newFile = { ...this.file, hunks: newHunks };
    callback(newFile);
  };

  getMaxOldLineNumber = (newChanges: Change[]) => {
    const lastChange = newChanges[newChanges.length - 1];
    return lastChange.oldLineNumber || lastChange.lineNumber!;
  };

  getMaxNewLineNumber = (newChanges: Change[]) => {
    const lastChange = newChanges[newChanges.length - 1];
    return lastChange.newLineNumber || lastChange.lineNumber!;
  };

  getHunk: (n: number) => ExpandableHunk = (n: number) => {
    return {
      maxExpandHeadRange: this.computeMaxExpandHeadRange(n),
      maxExpandBottomRange: this.computeMaxExpandBottomRange(n),
      expandHead: (count: number, callback: (newFile: File) => void) => this.expandHead(n, count, callback),
      expandBottom: (count: number, callback: (newFile: File) => void) => this.expandBottom(n, count, callback),
      hunk: this.file?.hunks![n]
    };
  };
}

export type ExpandableHunk = {
  hunk: Hunk;
  maxExpandHeadRange: number;
  maxExpandBottomRange: number;
  expandHead: (count: number, callback: (newFile: File) => void) => void;
  expandBottom: (count: number, callback: (newFile: File) => void) => void;
};

export default DiffExpander;

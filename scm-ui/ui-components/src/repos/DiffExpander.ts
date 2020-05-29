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
    return this.file.hunks![n]!.newStart! + this.file.hunks![n]!.newLines!;
  };

  computeMaxExpandHeadRange = (n: number) => {
    if (this.file.type === "delete") {
      return 0;
    } else if (n === 0) {
      return this.minLineNumber(n) - 1;
    }
    return this.minLineNumber(n) - this.maxLineNumber(n - 1);
  };

  computeMaxExpandBottomRange = (n: number) => {
    if (this.file.type === "add" || this.file.type === "delete") {
      return 0;
    } else if (n === this.file!.hunks!.length - 1) {
      return Number.MAX_SAFE_INTEGER;
    }
    return this.minLineNumber(n + 1) - this.maxLineNumber(n);
  };

  expandHead = (n: number, callback: (newFile: File) => void) => {
    const lineRequestUrl = this.file._links.lines.href
      .replace("{start}", this.minLineNumber(n) - Math.min(10, this.computeMaxExpandHeadRange(n)))
      .replace("{end}", this.minLineNumber(n) - 1);
    apiClient
      .get(lineRequestUrl)
      .then(response => response.text())
      .then(text => text.split("\n"))
      .then(lines => this.expandHunkAtHead(n, lines, callback));
  };

  expandBottom = (n: number, callback: (newFile: File) => void) => {
    const lineRequestUrl = this.file._links.lines.href
      .replace("{start}", this.maxLineNumber(n) + 1)
      .replace("{end}", this.maxLineNumber(n) + Math.min(10, this.computeMaxExpandBottomRange(n)));
    apiClient
      .get(lineRequestUrl)
      .then(response => response.text())
      .then(text => text.split("\n"))
      .then(lines => this.expandHunkAtBottom(n, lines, callback));
  };

  expandHunkAtHead = (n: number, lines: string[], callback: (newFile: File) => void) => {
    const hunk = this.file.hunks[n];
    const newChanges: Change[] = [];
    let oldLineNumber = hunk.changes[0].oldLineNumber - lines.length;
    let newLineNumber = hunk.changes[0].newLineNumber - lines.length;

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
      oldStart: hunk.oldStart - lines.length,
      newStart: hunk.newStart - lines.length,
      oldLines: hunk.oldLines + lines.length,
      newLines: hunk.newLines + lines.length,
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

  expandHunkAtBottom = (n: number, lines: string[], callback: (newFile: File) => void) => {
    const hunk = this.file.hunks![n];
    const newChanges = [...hunk.changes];
    let oldLineNumber = newChanges[newChanges.length - 1].oldLineNumber;
    let newLineNumber = newChanges[newChanges.length - 1].newLineNumber;

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
      oldLines: hunk.oldLines + lines.length,
      newLines: hunk.newLines + lines.length,
      changes: newChanges
    };
    const newHunks: Hunk[] = [];
    this.file.hunks.forEach((oldHunk: Hunk, i: number) => {
      if (i === n) {
        newHunks.push(newHunk);
      } else {
        newHunks.push(oldHunk);
      }
    });
    const newFile = { ...this.file, hunks: newHunks };
    callback(newFile);
  };

  getHunk: (n: number) => ExpandableHunk = (n: number) => {
    return {
      maxExpandHeadRange: this.computeMaxExpandHeadRange(n),
      maxExpandBottomRange: this.computeMaxExpandBottomRange(n),
      expandHead: (callback: (newFile: File) => void) => this.expandHead(n, callback),
      expandBottom: (callback: (newFile: File) => void) => this.expandBottom(n, callback),
      hunk: this.file?.hunks![n]
    };
  };
}

export type ExpandableHunk = {
  hunk: Hunk;
  maxExpandHeadRange: number;
  maxExpandBottomRange: number;
  expandHead: (callback: (newFile: File) => void) => void;
  expandBottom: (callback: (newFile: File) => void) => void;
};

export default DiffExpander;

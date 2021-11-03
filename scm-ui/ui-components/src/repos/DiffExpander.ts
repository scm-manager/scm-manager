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
import { Change, FileDiff, Hunk, Link } from "@scm-manager/ui-types";

class DiffExpander {
  file: FileDiff;

  constructor(file: FileDiff) {
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
    if (changes[changes.length - 1]?.type === "normal") {
      if (n === this.file!.hunks!.length - 1) {
        return this.file!.hunks![this.file!.hunks!.length - 1].fullyExpanded ? 0 : -1;
      }
      return this.minLineNumber(n + 1) - this.maxLineNumber(n) - 1;
    } else {
      return 0;
    }
  };

  expandHead: (n: number, count: number) => Promise<FileDiff> = (n, count) => {
    const start = this.minLineNumber(n) - Math.min(count, this.computeMaxExpandHeadRange(n)) - 1;
    const end = this.minLineNumber(n) - 1;
    return this.loadLines(start, end).then((lines) => {
      const hunk = this.file.hunks![n];

      const newHunk = this.createNewHunk(
        hunk.oldStart! - lines.length,
        hunk.newStart! - lines.length,
        lines,
        lines.length
      );

      return this.addHunkToFile(newHunk, n);
    });
  };

  expandBottom: (n: number, count: number) => Promise<FileDiff> = (n, count) => {
    const maxExpandBottomRange = this.computeMaxExpandBottomRange(n);
    const start = this.maxLineNumber(n);
    const end =
      count > 0
        ? start + Math.min(count, maxExpandBottomRange > 0 ? maxExpandBottomRange : Number.MAX_SAFE_INTEGER)
        : -1;
    return this.loadLines(start, end).then((lines) => {
      const hunk = this.file.hunks![n];

      const newHunk: Hunk = this.createNewHunk(
        this.getMaxOldLineNumber(hunk.changes) + 1,
        this.getMaxNewLineNumber(hunk.changes) + 1,
        lines,
        count
      );

      return this.addHunkToFile(newHunk, n + 1);
    });
  };

  loadLines = (start: number, end: number) => {
    const lineRequestUrl = (this.file._links!.lines as Link).href
      .replace("{start}", start.toString())
      .replace("{end}", end.toString());
    return apiClient
      .get(lineRequestUrl)
      .then((response) => response.text())
      .then((text) => text.split("\n"))
      .then((lines) => (lines[lines.length - 1] === "" ? lines.slice(0, lines.length - 1) : lines));
  };

  addHunkToFile = (newHunk: Hunk, position: number) => {
    const newHunks: Hunk[] = [];
    this.file.hunks!.forEach((oldHunk: Hunk, i: number) => {
      if (i === position) {
        newHunks.push(newHunk);
      }
      newHunks.push(oldHunk);
    });
    if (position === newHunks.length) {
      newHunks.push(newHunk);
    }
    return { ...this.file, hunks: newHunks };
  };

  createNewHunk = (oldFirstLineNumber: number, newFirstLineNumber: number, lines: string[], requestedLines: number) => {
    const newChanges: Change[] = [];

    let oldLineNumber: number = oldFirstLineNumber;
    let newLineNumber: number = newFirstLineNumber;

    lines.forEach((line) => {
      newChanges.push({
        content: line,
        type: "normal",
        oldLineNumber,
        newLineNumber,
        isNormal: true,
      });
      oldLineNumber += 1;
      newLineNumber += 1;
    });

    return {
      changes: newChanges,
      content: "",
      oldStart: oldFirstLineNumber,
      newStart: newFirstLineNumber,
      oldLines: lines.length,
      newLines: lines.length,
      expansion: true,
      fullyExpanded: requestedLines < 0 || lines.length < requestedLines,
    };
  };

  getMaxOldLineNumber = (newChanges: Change[]) => {
    const lastChange = newChanges[newChanges.length - 1];
    return lastChange.oldLineNumber || lastChange.lineNumber!;
  };

  getMaxNewLineNumber = (newChanges: Change[]) => {
    const lastChange = newChanges[newChanges.length - 1];
    return lastChange.newLineNumber || lastChange.lineNumber!;
  };

  getHunk: (n: number) => ExpandableHunk = (n) => {
    return {
      maxExpandHeadRange: this.computeMaxExpandHeadRange(n),
      maxExpandBottomRange: this.computeMaxExpandBottomRange(n),
      expandHead: (count: number) => this.expandHead(n, count),
      expandBottom: (count: number) => this.expandBottom(n, count),
      hunk: this.file?.hunks![n],
    };
  };
}

export type ExpandableHunk = {
  hunk: Hunk;
  maxExpandHeadRange: number;
  maxExpandBottomRange: number;
  expandHead: (count: number) => Promise<FileDiff>;
  expandBottom: (count: number) => Promise<FileDiff>;
};

export default DiffExpander;

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

import { File, Hunk } from "./DiffTypes";

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

  getHunk: (n: number) => ExpandableHunk = (n: number) => {
    return {
      maxExpandHeadRange: this.computeMaxExpandHeadRange(n),
      maxExpandBottomRange: this.computeMaxExpandBottomRange(n),
      expandHead: () => {
        return this;
      },
      expandBottom: () => {
        return this;
      },
      hunk: this.file?.hunks![n]
    };
  };
}

export type ExpandableHunk = {
  hunk: Hunk;
  maxExpandHeadRange: number;
  maxExpandBottomRange: number;
  expandHead: () => DiffExpander;
  expandBottom: () => DiffExpander;
};

export default DiffExpander;

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

import { File, FileChangeType, Hunk } from "./DiffTypes";
import { getPath, createHunkIdentifier, createHunkIdentifierFromContext, escapeWhitespace } from "./diffs";

describe("tests for diff util functions", () => {
  const file = (type: FileChangeType, oldPath: string, newPath: string): File => {
    return {
      hunks: [],
      type: type,
      oldPath,
      newPath,
      newEndingNewLine: true,
      oldEndingNewLine: true
    };
  };

  const add = (path: string) => {
    return file("add", "/dev/null", path);
  };

  const rm = (path: string) => {
    return file("delete", path, "/dev/null");
  };

  const modify = (path: string) => {
    return file("modify", path, path);
  };

  const createHunk = (content: string): Hunk => {
    return {
      content,
      changes: []
    };
  };

  describe("getPath tests", () => {
    it("should pick the new path, for type add", () => {
      const file = add("/etc/passwd");
      const path = getPath(file);
      expect(path).toBe("/etc/passwd");
    });

    it("should pick the old path, for type delete", () => {
      const file = rm("/etc/passwd");
      const path = getPath(file);
      expect(path).toBe("/etc/passwd");
    });
  });

  describe("createHunkIdentifier tests", () => {
    it("should create identifier", () => {
      const file = modify("/etc/passwd");
      const hunk = createHunk("@@ -1,18 +1,15 @@");
      const identifier = createHunkIdentifier(file, hunk);
      expect(identifier).toBe("modify_/etc/passwd_@@ -1,18 +1,15 @@");
    });
  });

  describe("createHunkIdentifierFromContext tests", () => {
    it("should create identifier", () => {
      const identifier = createHunkIdentifierFromContext({
        file: rm("/etc/passwd"),
        hunk: createHunk("@@ -1,42 +1,39 @@")
      });
      expect(identifier).toBe("delete_/etc/passwd_@@ -1,42 +1,39 @@");
    });
  });

  describe("escapeWhitespace tests", () => {
    it("should escape whitespaces", () => {
      const escaped = escapeWhitespace("spaceship hog");
      expect(escaped).toBe("spaceship-hog");
    });
    it("should escape multiple whitespaces", () => {
      const escaped = escapeWhitespace("spaceship heart of gold");
      expect(escaped).toBe("spaceship-heart-of-gold");
    });
  });
});

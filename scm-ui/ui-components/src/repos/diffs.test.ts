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

import { FileChangeType, Hunk, FileDiff } from "@scm-manager/ui-types";
import { getPath, createHunkIdentifier, createHunkIdentifierFromContext, escapeWhitespace } from "./diffs";

describe("tests for diff util functions", () => {
  const file = (type: FileChangeType, oldPath: string, newPath: string): FileDiff => {
    return {
      hunks: [],
      type: type,
      oldPath,
      newPath,
      newEndingNewLine: true,
      oldEndingNewLine: true,
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
      changes: [],
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
        hunk: createHunk("@@ -1,42 +1,39 @@"),
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

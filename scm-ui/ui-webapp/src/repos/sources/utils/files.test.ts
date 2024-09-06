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

import { File } from "@scm-manager/ui-types";
import { isEmptyDirectory, isRootFile, isRootPath } from "./files";

describe("files tests", () => {
  const createRootDirectory = (): File => {
    return {
      name: "",
      path: "/",
      revision: "42",
      directory: true,
      _links: {},
      _embedded: {
        children: []
      }
    };
  };

  const readme: File = {
    name: "README.md",
    path: "README.md",
    revision: "42",
    directory: false,
    _links: {}
  };

  describe("isRootPath tests", () => {
    it("should return false", () => {
      const paths = ["a", "b/c", "/a"];
      for (const p of paths) {
        expect(isRootPath(p)).toBe(false);
      }
    });

    it("should return true", () => {
      const paths = ["", "/"];
      for (const p of paths) {
        expect(isRootPath(p)).toBe(true);
      }
    });
  });

  describe("isRootFile tests", () => {
    it("should return false, if it is not a directory", () => {
      const file = createRootDirectory();
      file.directory = false;
      expect(isRootFile(file)).toBe(false);
    });

    it("should return true", () => {
      const directory = createRootDirectory();
      expect(isRootFile(directory)).toBe(true);
    });
  });

  describe("isEmptyDirectory tests", () => {
    it("should return false, if it is not a directory", () => {
      const directory = createRootDirectory();
      directory.directory = false;
      directory._embedded.children = [readme];
      expect(isEmptyDirectory(directory)).toBe(false);
    });

    it("should return false, if it is not empty", () => {
      const directory = createRootDirectory();
      directory._embedded.children = [readme];
      expect(isEmptyDirectory(directory)).toBe(false);
    });

    it("should return true, if children is empty", () => {
      const directory = createRootDirectory();
      expect(isEmptyDirectory(directory)).toBe(true);
    });

    it("should return true, if children is undefined", () => {
      const directory = createRootDirectory();
      directory._embedded.children = undefined;
      expect(isEmptyDirectory(directory)).toBe(true);
    });

    it("should return true, if _embedded is undefined", () => {
      const directory = createRootDirectory();
      directory._embedded = undefined;
      expect(isEmptyDirectory(directory)).toBe(true);
    });
  });
});

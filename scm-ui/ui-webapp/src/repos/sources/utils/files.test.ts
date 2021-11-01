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
        children: [],
      },
    };
  };

  const readme: File = {
    name: "README.md",
    path: "README.md",
    revision: "42",
    directory: false,
    _links: {},
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

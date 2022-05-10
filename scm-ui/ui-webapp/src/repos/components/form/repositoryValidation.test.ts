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

import * as validator from "./repositoryValidation";

describe("repository name validation", () => {
  // we don't need rich tests, because they are in validation.test.js
  it("should validate the name", () => {
    expect(validator.isNameValid("scm-manager")).toBe(true);
  });

  it("should fail for old nested repository names", () => {
    // in v2 this is not allowed
    expect(validator.isNameValid("scm/manager")).toBe(false);
    expect(validator.isNameValid("scm/ma/nager")).toBe(false);
  });

  it("should allow same names as the backend", () => {
    const validPaths = ["scm", "scm.gitz", "s", "sc", ".hiddenrepo", "b.", "...", "..c", "d..", "a..c"];

    validPaths.forEach((path) => expect(validator.isNameValid(path)).toBe(true));
  });

  it("should deny same names as the backend", () => {
    const invalidPaths = [
      ".",
      "/",
      "//",
      "..",
      "/.",
      "/..",
      "./",
      "../",
      "/../",
      "/./",
      "/...",
      "/abc",
      ".../",
      "/sdf/",
      "asdf/",
      "./b",
      "scm/plugins/.",
      "scm/../plugins",
      "scm/main/",
      "/scm/main/",
      "scm/./main",
      "scm//main",
      "scm\\main",
      "scm/main-$HOME",
      // eslint-disable-next-line no-template-curly-in-string
      "scm/main-${HOME}-home",
      "scm/main-%HOME-home",
      "scm/main-%HOME%-home",
      "abc$abc",
      "abc%abc",
      "abc<abc",
      "abc>abc",
      "abc#abc",
      "abc+abc",
      "abc{abc",
      "abc}abc",
      "abc(abc",
      "abc)abc",
      "abc[abc",
      "abc]abc",
      "abc|abc",
      "scm/main",
      "scm/plugins/git-plugin",
      ".scm/plugins",
      "a/b..",
      "a/..b",
      "scm/main",
      "scm/plugins/git-plugin",
      "scm/plugins/git-plugin",
      "scm.git",
    ];

    invalidPaths.forEach((path) => expect(validator.isNameValid(path)).toBe(false));
  });
});

describe("repository contact validation", () => {
  it("should allow empty contact", () => {
    expect(validator.isContactValid("")).toBe(true);
  });

  // we don't need rich tests, because they are in validation.test.js
  it("should allow real mail addresses", () => {
    expect(validator.isContactValid("trici.mcmillian@hitchhiker.com")).toBe(true);
  });

  it("should fail on invalid mail addresses", () => {
    expect(validator.isContactValid("tricia")).toBe(false);
  });
});

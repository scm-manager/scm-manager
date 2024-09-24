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

    validPaths.forEach(path => expect(validator.isNameValid(path)).toBe(true));
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
      "scm.git"
    ];

    invalidPaths.forEach(path => expect(validator.isNameValid(path)).toBe(false));
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

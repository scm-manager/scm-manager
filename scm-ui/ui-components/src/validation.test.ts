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
///
/// MIT License
///
/// Copyright (c) 2020-present Cloudogu GmbH and Contributors
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
///

import * as validator from "./validation";

describe("test name validation", () => {
  // invalid names taken from ValidationUtilTest.java
  const invalidNames = [
    "@test",
    " test 123",
    " test 123 ",
    "test 123 ",
    "test/123",
    "test%123",
    "test:123",
    "t ",
    " t",
    " t ",
    "",
    " invalid_name",
    "another%one",
    "!!!",
    "!_!"
  ];
  for (const name of invalidNames) {
    it(`should return false for '${name}'`, () => {
      expect(validator.isNameValid(name)).toBe(false);
    });
  }

  // valid names taken from ValidationUtilTest.java
  const validNames = [
    "test",
    "test.git",
    "Test123.git",
    "Test123-git",
    "Test_user-123.git",
    "test@scm-manager.de",
    "test123",
    "tt",
    "t",
    "valid_name",
    "another1",
    "stillValid",
    "this.one_as-well",
    "and@this"
  ];
  for (const name of validNames) {
    it(`should return true for '${name}'`, () => {
      expect(validator.isNameValid(name)).toBe(true);
    });
  }
});

describe("test mail validation", () => {
  // invalid taken from ValidationUtilTest.java
  const invalid = [
    "ostfalia.de",
    "@ostfalia.de",
    "s.sdorra@",
    "s.sdorra@ostfalia",
    "s.sdorra@ ostfalia.de",
    "s.sdorra@[ostfalia.de"
  ];
  for (const mail of invalid) {
    it(`should return false for '${mail}'`, () => {
      expect(validator.isMailValid(mail)).toBe(false);
    });
  }

  // valid taken from ValidationUtilTest.java
  const valid = [
    "s.sdorra@ostfalia.de",
    "sdorra@ostfalia.de",
    "s.sdorra@hbk-bs.de",
    "s.sdorra@gmail.com",
    "s.sdorra@t.co",
    "s.sdorra@ucla.college",
    "s.sdorra@example.xn--p1ai",
    "s.sdorra@scm.solutions",
    "s'sdorra@scm.solutions",
    '"S Sdorra"@scm.solutions'
  ];
  for (const mail of valid) {
    it(`should return true for '${mail}'`, () => {
      expect(validator.isMailValid(mail)).toBe(true);
    });
  }
});

describe("test number validation", () => {
  const invalid = ["1a", "35gu", "dj6", "45,5", "test"];
  for (const number of invalid) {
    it(`should return false for '${number}'`, () => {
      expect(validator.isNumberValid(number)).toBe(false);
    });
  }
  const valid = ["1", "35", "2", "235", "34.4"];
  for (const number of valid) {
    it(`should return true for '${number}'`, () => {
      expect(validator.isNumberValid(number)).toBe(true);
    });
  }
});

describe("test path validation", () => {
  const invalid = ["//", "some//path", "end//"];
  for (const path of invalid) {
    it(`should return false for '${path}'`, () => {
      expect(validator.isPathValid(path)).toBe(false);
    });
  }
  const valid = ["", "/", "dir", "some/path", "end/"];
  for (const path of valid) {
    it(`should return true for '${path}'`, () => {
      expect(validator.isPathValid(path)).toBe(true);
    });
  }
});

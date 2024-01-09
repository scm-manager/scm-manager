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

import * as validator from "./validation";

describe("test name validation", () => {
  // invalid names taken from ValidationUtilTest.java
  const invalidNames = [
    "@test",
    " test 123",
    " test 123 ",
    "test 123 ",
    "test/123",
    "test:123",
    "t ",
    " t",
    " t ",
    "",
    " invalid_name",
    "%",
    "test%name",
    "test\\name",
  ];
  for (const name of invalidNames) {
    it(`should return false for '${name}'`, () => {
      expect(validator.isNameValid(name)).toBe(false);
    });
  }

  // valid names taken from ValidationUtilTest.java
  const validNames = [
    "test",
    "test git",
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
    "and@this",
    "Лорем-ипсум",
    "Λορεμ.ιπσθμ",
    "լոռեմիպսում",
    "ლორემიფსუმ",
    "प्रमान",
    "詳性約",
    "隠サレニ",
    "법률",
    "المدن",
    "אחד",
    "Hu-rëm",
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
    "s.sdorra@@ostfalia.de",
    "s.sdorra@ ostfalia.de",
    "s.sdorra @ostfalia.de",
    "s.sdorra@[ostfalia.de",
    "abc.example.com",
    "a@b@c@example.com"
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
    '"S Sdorra"@scm.solutions',
    "A@BC.DE",
    "x@example.com",
    "example@s.example",
    "user.name+tag+sorting@example.com",
    "name/surname@example.com",
    "user%example.com@example.org"
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
  const invalid = ["//", "some//path", "end//", ".", "..", "../"];
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

describe("test url validation", () => {
  const invalid = [
    "http://",
    "http://.",
    "http://..",
    "http://../",
    "http://?",
    "http://??",
    "http://??/",
    "http://#",
    "http://##",
    "http://##/",
    "http://foo.bar?q=Spaces should be encoded",
    "//",
    "//a",
    "///a",
    "///",
    "foo.com",
    "http:// shouldfail.com",
    ":// should fail",
    "http://foo.bar/foo(bar)baz quux",
    "http://.www.foo.bar/",
    "http://.www.foo.bar./",
  ];
  for (const url of invalid) {
    it(`should return false for '${url}'`, () => {
      expect(validator.isUrlValid(url)).toBe(false);
    });
  }
  const valid = [
    "ftps://foo.bar/",
    "h://test",
    "rdar://1234",
    "file:///blah/index.html",
    "https://foo.com/blah_blah",
    "ssh://foo.com/blah_blah",
    "https://foo.com/blah_blah/",
    "https://foo.com/blah_blah_(wikipedia)",
    "https://foo.com/blah_blah_(wikipedia)_(again)",
    "http://www.example.com/wpstyle/?p=364",
    "http://foo.com/blah_blah",
    "http://foo.com/blah_blah/",
    "http://foo.com/blah_blah_(wikipedia)",
    "http://foo.com/blah_blah_(wikipedia)_(again)",
    "http://www.example.com/wpstyle/?p=364",
    "https://www.example.com/foo/?bar=baz&inga=42&quux",
    "http://✪df.ws/123",
    "http://userid:password@example.com:8080",
    "http://userid:password@example.com:8080/",
    "http://userid@example.com",
    "http://userid@example.com/",
    "http://userid@example.com:8080",
    "http://userid@example.com:8080/",
    "http://userid:password@example.com",
    "http://userid:password@example.com/",
    "http://142.42.1.1/",
    "http://142.42.1.1:8080/",
    "http://➡.ws/䨹",
    "http://⌘.ws",
    "http://⌘.ws/",
    "http://foo.com/blah_(wikipedia)#cite-1",
    "http://foo.com/blah_(wikipedia)_blah#cite-1",
    "http://foo.com/unicode_(✪)_in_parens",
    "http://foo.com/(something)?after=parens",
    "http://☺.damowmow.com/",
    "http://code.google.com/events/#&product=browser",
    "http://j.mp",
    "http://foo.bar/?q=Test%20URL-encoded%20stuff",
    "http://مثال.إختبار",
    "http://例子.测试",
    "http://उदाहरण.परीक्षा",
    "http://-.~_!$&'()*+,;=:%40:80%2f::::::@example.com",
    "http://1337.net",
    "http://a.b-c.de",
    "http://223.255.255.254",
    "http://0.0.0.0",
  ];
  for (const url of valid) {
    it(`should return true for '${url}'`, () => {
      expect(validator.isUrlValid(url)).toBe(true);
    });
  }
});

describe("test filename validation", () => {
  const invalid = ["", "/", "some/file", ".", "..", "../", "\\", "\\name", "file:some"];
  for (const filename of invalid) {
    it(`should return false for '${filename}'`, () => {
      expect(validator.isFilenameValid(filename)).toBe(false);
    });
  }
  const valid = ["a", "test", "some_file", "end.txt", ".gitignore"];
  for (const filename of valid) {
    it(`should return true for '${filename}'`, () => {
      expect(validator.isFilenameValid(filename)).toBe(true);
    });
  }
});

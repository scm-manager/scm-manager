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

import { concat, getPageFromMatch, getQueryStringFromLocation, withEndingSlash } from "./urls";

describe("tests for withEndingSlash", () => {
  it("should append missing slash", () => {
    expect(withEndingSlash("abc")).toBe("abc/");
  });

  it("should not append a second slash", () => {
    expect(withEndingSlash("abc/")).toBe("abc/");
  });
});

describe("concat tests", () => {
  it("should concat the parts to a single url", () => {
    expect(concat("a")).toBe("a");
    expect(concat("a", "b")).toBe("a/b");
    expect(concat("a", "b", "c")).toBe("a/b/c");
  });
});

describe("tests for getPageFromMatch", () => {
  function createMatch(page: string) {
    return {
      params: {
        page
      }
    };
  }

  it("should return 1 for NaN", () => {
    const match = createMatch("any");
    expect(getPageFromMatch(match)).toBe(1);
  });

  it("should return 1 for 0", () => {
    const match = createMatch("0");
    expect(getPageFromMatch(match)).toBe(1);
  });

  it("should return the given number", () => {
    const match = createMatch("42");
    expect(getPageFromMatch(match)).toBe(42);
  });
});

describe("tests for getQueryStringFromLocation", () => {
  function createLocation(search: string) {
    return {
      search
    };
  }

  it("should return the query string", () => {
    const location = createLocation("?q=abc");
    expect(getQueryStringFromLocation(location)).toBe("abc");
  });

  it("should return query string from multiple parameters", () => {
    const location = createLocation("?x=a&y=b&q=abc&z=c");
    expect(getQueryStringFromLocation(location)).toBe("abc");
  });

  it("should return undefined if q is not available", () => {
    const location = createLocation("?x=a&y=b&z=c");
    expect(getQueryStringFromLocation(location)).toBeUndefined();
  });
});

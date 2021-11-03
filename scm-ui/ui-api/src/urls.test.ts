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

import { concat, getNamespaceAndPageFromMatch, getQueryStringFromLocation, withEndingSlash } from "./urls";

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

describe("tests for getNamespaceAndPageFromMatch", () => {
  function createMatch(namespace?: string, page?: string) {
    return {
      params: { namespace, page },
    };
  }

  it("should return no namespace and page 1 for neither namespace nor page", () => {
    const match = createMatch();
    expect(getNamespaceAndPageFromMatch(match)).toEqual({ namespace: undefined, page: 1 });
  });

  it("should return no namespace and page 1 for 0 as only parameter", () => {
    const match = createMatch("0");
    expect(getNamespaceAndPageFromMatch(match)).toEqual({ namespace: undefined, page: 1 });
  });

  it("should return no namespace and given number as page, when only a number is given", () => {
    const match = createMatch("42");
    expect(getNamespaceAndPageFromMatch(match)).toEqual({ namespace: undefined, page: 42 });
  });

  it("should return big number as namespace and page 1, when only a big number is given", () => {
    const match = createMatch("1337");
    expect(getNamespaceAndPageFromMatch(match)).toEqual({ namespace: "1337", page: 1 });
  });

  it("should namespace and page 1, when only a string is given", () => {
    const match = createMatch("something");
    expect(getNamespaceAndPageFromMatch(match)).toEqual({ namespace: "something", page: 1 });
  });

  it("should namespace and given page, when namespace and page are given", () => {
    const match = createMatch("something", "42");
    expect(getNamespaceAndPageFromMatch(match)).toEqual({ namespace: "something", page: 42 });
  });
});

describe("tests for getQueryStringFromLocation", () => {
  function createLocation(search: string) {
    return {
      search,
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

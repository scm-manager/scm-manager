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

import {
  concat,
  createPrevSourcePathQuery,
  getNamespaceAndPageFromMatch,
  getPrevSourcePathFromLocation,
  getQueryStringFromLocation,
  getValueStringFromLocationByKey,
  withEndingSlash,
} from "./urls";

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

  it("should return the query string with a space instead of a plus symbol", () => {
    const location = createLocation("?q=+(Test)");
    expect(getQueryStringFromLocation(location)).toBe(" (Test)");
  });

  it("should return the query string with a plus symbol", () => {
    const location = createLocation("?q=%2B(Test)");
    expect(getQueryStringFromLocation(location)).toBe("+(Test)");
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

describe("tests for getValueStringFromLocationByKey", () => {
  function createLocation(search: string) {
    return {
      search,
    };
  }

  it("should return the value string", () => {
    const location = createLocation("?name=abc");
    expect(getValueStringFromLocationByKey(location, "name")).toBe("abc");
  });

  it("should return value string from multiple parameters", () => {
    const location = createLocation("?x=a&y=b&q=abc&z=c");
    expect(getValueStringFromLocationByKey(location, "x")).toBe("a");
    expect(getValueStringFromLocationByKey(location, "y")).toBe("b");
    expect(getValueStringFromLocationByKey(location, "z")).toBe("c");
  });

  it("should return undefined if q is not available", () => {
    const location = createLocation("?x=a&y=b&z=c");
    expect(getValueStringFromLocationByKey(location, "namespace")).toBeUndefined();
  });
});

describe("tests for getPrevSourcePathFromLocation", () => {
  it("should return the value string", () => {
    const location = { search: "?prevSourcePath=src%2Fsub%25%2Ffile%26%252F.abc" };
    expect(getPrevSourcePathFromLocation(location)).toBe("src/sub%/file&%2F.abc");
  });

  it("should return undefined, because query parameter is missing", () => {
    const location = { search: "?q=abc" };
    expect(getPrevSourcePathFromLocation(location)).toBeUndefined();
  });

  it("should return undefined, because query parameter is missing", () => {
    const location = { search: "?q=abc" };
    expect(getPrevSourcePathFromLocation(location)).toBeUndefined();
  });
});

describe("tests for createPrevSourcePathQuery", () => {
  it("should return empty string if file path is empty", () => {
    const encodedPath = createPrevSourcePathQuery("");
    expect(encodedPath).toBe("");
  });

  it("should return the encoded path as query parameter", () => {
    const encodedPath = createPrevSourcePathQuery("src/sub%/file&%2F.abc");
    expect(encodedPath).toBe("prevSourcePath=src%2Fsub%25%2Ffile%26%252F.abc");
  });
});

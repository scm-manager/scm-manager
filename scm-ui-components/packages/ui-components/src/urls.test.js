// @flow
import {concat, getPageFromMatch, withEndingSlash} from "./urls";

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

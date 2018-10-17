import { getPageFromMatch } from "./Changesets";

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

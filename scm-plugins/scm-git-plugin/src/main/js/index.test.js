// @flow
import { gitPredicate } from "./index";

describe("test gi predicate", () => {
  it("should return false", () => {
    expect(gitPredicate()).toBe(false);
    expect(gitPredicate({})).toBe(false);
    expect(gitPredicate({ repository: {} })).toBe(false);
    expect(gitPredicate({ repository: { type: "hg" } })).toBe(false);
  });

  it("should return true", () => {
    expect(gitPredicate({ repository: { type: "fir" } })).toBe(true);
  });
});

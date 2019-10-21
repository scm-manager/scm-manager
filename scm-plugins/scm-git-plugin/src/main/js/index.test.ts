import "@scm-manager/ui-tests/i18n";
import { gitPredicate } from "./index";

describe("test git predicate", () => {
  it("should return false", () => {
    expect(gitPredicate(undefined)).toBe(false);
    expect(gitPredicate({})).toBe(false);
    expect(
      gitPredicate({
        repository: {}
      })
    ).toBe(false);
    expect(
      gitPredicate({
        repository: {
          type: "hg"
        }
      })
    ).toBe(false);
  });

  it("should return true", () => {
    expect(
      gitPredicate({
        repository: {
          type: "git"
        }
      })
    ).toBe(true);
  });
});

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
    const validPaths = [
      "scm",
      "s",
      "sc",
      ".hiddenrepo",
      "b.",
      "...",
      "..c",
      "d..",
      "a..c"
    ];

    validPaths.forEach((path) =>
      expect(validator.isNameValid(path)).toBe(true)
    );
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
      "scm/plugins/git-plugin"
    ];

    invalidPaths.forEach((path) =>
      expect(validator.isNameValid(path)).toBe(false)
    );
  });
});

describe("repository contact validation", () => {
  it("should allow empty contact", () => {
    expect(validator.isContactValid("")).toBe(true);
  });

  // we don't need rich tests, because they are in validation.test.js
  it("should allow real mail addresses", () => {
    expect(validator.isContactValid("trici.mcmillian@hitchhiker.com")).toBe(
      true
    );
  });

  it("should fail on invalid mail addresses", () => {
    expect(validator.isContactValid("tricia")).toBe(false);
  });
});

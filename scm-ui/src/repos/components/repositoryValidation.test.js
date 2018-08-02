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

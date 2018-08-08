// @flow
import * as validator from "./validation";

describe("test name validation", () => {
  it("should return false", () => {
    // invalid names taken from ValidationUtilTest.java
    const invalidNames = [
      " test 123",
      " test 123 ",
      "test 123 ",
      "test/123",
      "test%123",
      "test:123",
      "t ",
      " t",
      " t ",
      "",

      " invalid_name",
      "another%one",
      "!!!",
      "!_!"
    ];
    for (let name of invalidNames) {
      expect(validator.isNameValid(name)).toBe(false);
    }
  });

  it("should return true", () => {
    // valid names taken from ValidationUtilTest.java
    const validNames = [
      "test",
      "test.git",
      "Test123.git",
      "Test123-git",
      "Test_user-123.git",
      "test@scm-manager.de",
      "test 123",
      "tt",
      "t",

      "valid_name",
      "another1",
      "stillValid",
      "this.one_as-well",
      "and@this"
    ];
    for (let name of validNames) {
      expect(validator.isNameValid(name)).toBe(true);
    }
  });
});

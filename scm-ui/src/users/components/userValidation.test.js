// @flow
import * as validator from "./userValidation";

describe("test displayName validation", () => {
  it("should return false", () => {
    expect(validator.isDisplayNameValid("")).toBe(false);
  });

  it("should return true", () => {
    // valid names taken from ValidationUtilTest.java
    const validNames = [
      "Arthur Dent",
      "Tricia.McMillan@hitchhiker.com",
      "Ford Prefect (ford.prefect@hitchhiker.com)",
      "Zaphod Beeblebrox <zaphod.beeblebrox@hitchhiker.com>",
      "Marvin, der depressive Roboter"
    ];
    for (let name of validNames) {
      expect(validator.isDisplayNameValid(name)).toBe(true);
    }
  });
});

describe("test password validation", () => {
  it("should return false", () => {
    // invalid taken from ValidationUtilTest.java
    const invalid = ["", "abc", "aaabbbcccdddeeefffggghhhiiijjjkkk"];
    for (let password of invalid) {
      expect(validator.isPasswordValid(password)).toBe(false);
    }
  });

  it("should return true", () => {
    // valid taken from ValidationUtilTest.java
    const valid = ["secret123", "mySuperSecretPassword"];
    for (let password of valid) {
      expect(validator.isPasswordValid(password)).toBe(true);
    }
  });
});

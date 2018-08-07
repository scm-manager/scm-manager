//@flow
import * as validator from "./groupValidation";

describe("validator", () => {
  it("should validate allowed names correctly", () => {
    const validNames = ["valid_name", "another1", "stillValid", "this.one_as-well", "and@this"];

    for (let validName of validNames) {
      expect(validator.isNameValid(validName)).toBeTruthy();
    }
  });

  it("should reject invalid names", () => {
    const invalidNames = [" invalid_name", "another%one", "!!!", "!_!"];

    for (let invalidName of invalidNames) {
      expect(validator.isNameValid(invalidName)).toBeFalsy();
    }
  })
});

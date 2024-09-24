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
      "Marvin, der depressive Roboter",
    ];
    for (const name of validNames) {
      expect(validator.isDisplayNameValid(name)).toBe(true);
    }
  });
});

describe("test password validation", () => {
  it("should return false", () => {
    // invalid taken from ValidationUtilTest.java
    const invalid = ["", "abc"];
    for (const password of invalid) {
      expect(validator.isPasswordValid(password)).toBe(false);
    }
  });

  it("should return true", () => {
    // valid taken from ValidationUtilTest.java
    const valid = ["secret123", "mySuperSecretPassword", "mySuperSecretPasswordWithMoreThan1024Characters"];
    for (const password of valid) {
      expect(validator.isPasswordValid(password)).toBe(true);
    }
  });
});

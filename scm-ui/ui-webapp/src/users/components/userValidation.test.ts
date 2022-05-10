/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
    const invalid = ["", "abc", "aaabbbcccdddeeefffggghhhiiijjjkkk"];
    for (const password of invalid) {
      expect(validator.isPasswordValid(password)).toBe(false);
    }
  });

  it("should return true", () => {
    // valid taken from ValidationUtilTest.java
    const valid = ["secret123", "mySuperSecretPassword"];
    for (const password of valid) {
      expect(validator.isPasswordValid(password)).toBe(true);
    }
  });
});

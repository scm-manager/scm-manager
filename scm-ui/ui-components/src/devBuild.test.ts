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

import { createAttributesForTesting, isDevBuild } from "./devBuild";

describe("devbuild tests", () => {
  let stage: string | undefined;

  const setStage = (s?: string) => {
    // @ts-ignore scmStage is set on the index page
    window.scmStage = s;
  };

  beforeAll(() => {
    // @ts-ignore scmStage is set on the index page
    stage = window.scmStage;
  });

  afterAll(() => {
    setStage(stage);
  });

  describe("isDevBuild tests", () => {
    it("should return true for development", () => {
      setStage("development");
      expect(isDevBuild()).toBe(true);
    });

    it("should return false for production", () => {
      setStage("production");
      expect(isDevBuild()).toBe(false);
    });
  });

  describe("createAttributesForTesting in development mode", () => {
    beforeAll(() => {
      setStage("development");
    });

    it("should return undefined for non development", () => {
      const attributes = createAttributesForTesting("123");
      expect(attributes).toBeDefined();
    });

    it("should return undefined for undefined testid", () => {
      const attributes = createAttributesForTesting();
      expect(attributes).toBeUndefined();
    });

    it("should remove spaces from test id", () => {
      const attributes = createAttributesForTesting("heart of gold");
      if (attributes) {
        expect(attributes["data-testid"]).toBe("heart-of-gold");
      } else {
        throw new Error("attributes should be defined");
      }
    });

    it("should lower case test id", () => {
      const attributes = createAttributesForTesting("HeartOfGold");
      if (attributes) {
        expect(attributes["data-testid"]).toBe("heartofgold");
      } else {
        throw new Error("attributes should be defined");
      }
    });
  });
});

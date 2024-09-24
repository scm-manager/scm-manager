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

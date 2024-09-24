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

import { createMatcher, filepathSearch } from "./filepathSearch";

describe("filepathSearch tests", () => {
  it("should match simple char sequence", () => {
    const matcher = createMatcher("hitch");
    expect(matcher("hitchhiker").matches).toBe(true);
    expect(matcher("trillian").matches).toBe(false);
  });

  it("should ignore case of path", () => {
    const matcher = createMatcher("hitch");
    expect(matcher("hiTcHhiker").matches).toBe(true);
  });

  it("should ignore case of query", () => {
    const matcher = createMatcher("HiTcH");
    expect(matcher("hitchhiker").matches).toBe(true);
  });

  it("should return sorted by score", () => {
    const paths = [
      "AccessSomething",
      "AccessTokenResolver",
      "SomethingDifferent",
      "SomeResolver",
      "SomeTokenResolver",
      "accesstokenresolver",
      "ActorExpression"
    ];

    const matches = filepathSearch(paths, "AcToRe");
    expect(matches).toEqual(["ActorExpression", "AccessTokenResolver", "accesstokenresolver"]);
  });

  it("should score path if filename not match", () => {
    const matcher = createMatcher("AcToRe");
    const match = matcher("src/main/ac/to/re/Main.java");
    expect(match.score).toBeGreaterThan(0);
  });

  it("should score higher if the name includes the query", () => {
    const matcher = createMatcher("Test");
    const one = matcher("src/main/js/types.ts");
    const two = matcher("src/test/java/com/cloudogu/scm/landingpage/myevents/PluginTestHelper.java");
    expect(two.score).toBeGreaterThan(one.score);
  });
});

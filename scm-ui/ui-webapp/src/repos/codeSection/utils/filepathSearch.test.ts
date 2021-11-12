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
      "ActorExpression",
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

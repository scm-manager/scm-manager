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

import { regExpPattern } from "./remarkCommitLinksParser";

describe("Remark Commit Links RegEx Tests", () => {
  it("should match simple names", () => {
    expect("namespace/name@1a5s4w8a".match(regExpPattern)).toBeTruthy();
  });
  it("should match complex names", () => {
    expect("hitchhiker/heart-of-gold@c7237cb60689046990dc9dc2a388a517adb3e2b2".match(regExpPattern)).toBeTruthy();
  });
  it("should replace match", () => {
    expect("Prefix namespace/name@42 suffix".replace(regExpPattern, "replaced")).toBe("Prefix replaced suffix");
  });
  it("should match groups", () => {
    const match = regExpPattern.exec("namespace/name@42");
    expect(match).toBeTruthy();
    if (match) {
      expect(match[1]).toBe("namespace");
      expect(match[2]).toBe("name");
      expect(match[3]).toBe("42");
    }
  });
  it("should match multiple links in text", () => {
    const text = "Prefix hitchhiker/heart-of-gold@42 some text hitchhiker/heart-of-gold@21 suffix";
    const matches = [];

    let match = regExpPattern.exec(text);
    while (match !== null) {
      matches.push(match[0]);
      match = regExpPattern.exec(text);
    }

    expect(matches[0]).toBe("hitchhiker/heart-of-gold@42");
    expect(matches[1]).toBe("hitchhiker/heart-of-gold@21");
  });
});

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

import { regExpPattern } from "./remarkChangesetShortLinkParser";

describe("Remark Commit Links RegEx Tests", () => {
  it("should match simple names", () => {
    const regExp = new RegExp(regExpPattern, "g");
    expect("namespace/name@1a5s4w8a".match(regExp)).toBeTruthy();
  });
  it("should match complex names", () => {
    const regExp = new RegExp(regExpPattern, "g");
    expect("hitchhiker/heart-of-gold@c7237cb60689046990dc9dc2a388a517adb3e2b2".match(regExp)).toBeTruthy();
  });
  it("should replace match", () => {
    const regExp = new RegExp(regExpPattern, "g");
    expect("Prefix namespace/name@42 suffix".replace(regExp, "replaced")).toBe("Prefix replaced suffix");
  });
  it("should match groups", () => {
    const regExp = new RegExp(regExpPattern, "g");
    const match = regExp.exec("namespace/name@42");
    expect(match).toBeTruthy();
    if (match) {
      expect(match[1]).toBe("namespace");
      expect(match[2]).toBe("name");
      expect(match[3]).toBe("42");
    }
  });
  it("should match multiple links in text", () => {
    const regExp = new RegExp(regExpPattern, "g");
    const text = "Prefix hitchhiker/heart-of-gold@42 some text hitchhiker/heart-of-gold@21 suffix";
    const matches = [];

    let match = regExp.exec(text);
    while (match !== null) {
      matches.push(match[0]);
      match = regExp.exec(text);
    }

    expect(matches[0]).toBe("hitchhiker/heart-of-gold@42");
    expect(matches[1]).toBe("hitchhiker/heart-of-gold@21");
  });
});

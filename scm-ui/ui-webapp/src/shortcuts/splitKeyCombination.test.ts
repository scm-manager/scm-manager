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

import splitKeyCombination from "./splitKeyCombination";

const MAC_USER_AGENT =
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/600.7.12 (KHTML, like Gecko) Version/8.0.7 Safari/600.7.12";
const WINDOWS_USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36";
const LINUX_USER_AGENT =
  "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36";
const NO_USER_AGENT = "";

describe("splitKeyCombination", () => {
  it("should split and replace correctly based on user agent", () => {
    expect(splitKeyCombination("alt+a meta+b meta+c", WINDOWS_USER_AGENT)).toEqual([
      "alt",
      "a",
      "meta",
      "b",
      "meta",
      "c",
    ]);
    expect(splitKeyCombination("option+a command+b command+c mod+d", LINUX_USER_AGENT)).toEqual([
      "alt",
      "a",
      "meta",
      "b",
      "meta",
      "c",
      "ctrl",
      "d",
    ]);
    expect(splitKeyCombination("alt+a meta+b mod+c", MAC_USER_AGENT)).toEqual(["⌥", "a", "⌘", "b", "⌘", "c"]);
  });

  it("should replace arrow key keywords with unicode characters", () => {
    expect(splitKeyCombination("up down left right", NO_USER_AGENT)).toEqual(["↑", "↓", "←", "→"]);
  });
});

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

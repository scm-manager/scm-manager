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

import { chooseLocale, supportedLocales } from "./useDateFormatter";

describe("test choose locale", () => {
  it("should choose de", () => {
    const locale = chooseLocale("de_DE", ["de", "en"]);
    expect(locale).toBe(supportedLocales.de);
  });

  it("should choose de, even without language array", () => {
    const locale = chooseLocale("de", []);
    expect(locale).toBe(supportedLocales.de);
  });

  it("should choose es", () => {
    const locale = chooseLocale("de", ["af", "be", "es"]);
    expect(locale).toBe(supportedLocales.es);
  });

  it("should fallback en", () => {
    const locale = chooseLocale("af", ["af", "be"]);
    expect(locale).toBe(supportedLocales.en);
  });
});

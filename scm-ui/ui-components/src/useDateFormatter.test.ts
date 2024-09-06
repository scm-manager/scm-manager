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

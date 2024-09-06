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

import { determineLanguage } from "./languages";

describe("syntax highlighter", () => {
  it("should return the language as it is", () => {
    const java = determineLanguage("java");
    expect(java).toBe("java");
  });

  it("should lower case the language", () => {
    const java = determineLanguage("Java");
    expect(java).toBe("java");
  });

  it("should return text if language is undefined", () => {
    const lang = determineLanguage();
    expect(lang).toBe("text");
  });

  it("should return text if language is an empty string", () => {
    const lang = determineLanguage("");
    expect(lang).toBe("text");
  });

});

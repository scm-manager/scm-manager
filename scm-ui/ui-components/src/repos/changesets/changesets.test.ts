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

import { parseDescription } from "./changesets";

describe("parseDescription tests", () => {
  it("should return a description with title and message", () => {
    const desc = parseDescription("Hello\nTrillian");
    expect(desc.title).toBe("Hello");
    expect(desc.message).toBe("Trillian");
  });

  it("should return a description with title and without message", () => {
    const desc = parseDescription("Hello Trillian");
    expect(desc.title).toBe("Hello Trillian");
  });

  it("should return an empty description for undefined", () => {
    const desc = parseDescription();
    expect(desc.title).toBe("");
    expect(desc.message).toBe("");
  });
});

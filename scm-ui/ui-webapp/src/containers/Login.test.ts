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

import { from } from "./Login";

describe("from tests", () => {
  it("should use default location", () => {
    const path = from("", {});
    expect(path).toBe("/");
  });

  it("should use default location without params", () => {
    const path = from();
    expect(path).toBe("/");
  });

  it("should use default location with null params", () => {
    const path = from("", null);
    expect(path).toBe("/");
  });

  it("should use location from query parameter", () => {
    const path = from("from=/repos", {});
    expect(path).toBe("/repos");
  });

  it("should use location from state", () => {
    const path = from("", { from: "/users" });
    expect(path).toBe("/users");
  });

  it("should prefer location from query parameter", () => {
    const path = from("from=/groups", { from: "/users" });
    expect(path).toBe("/groups");
  });

  it("should decode query param", () => {
    const path = from(`from=${encodeURIComponent("/admin/plugins/installed")}`);
    expect(path).toBe("/admin/plugins/installed");
  });
});

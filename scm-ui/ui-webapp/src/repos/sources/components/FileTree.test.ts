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

import { findParent } from "./FileTree";

describe("find parent tests", () => {
  it("should return the parent path", () => {
    expect(findParent("src/main/js/")).toBe("src/main");
    expect(findParent("src/main/js")).toBe("src/main");
    expect(findParent("src/main")).toBe("src");
    expect(findParent("src")).toBe("");
  });
});

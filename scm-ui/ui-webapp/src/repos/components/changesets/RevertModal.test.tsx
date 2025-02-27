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

import { getSelectedBranch } from "./RevertModal";

describe("getSelectedBranch", () => {
  it("should return the correct branch from a query", () => {
    const output = getSelectedBranch({ branch: "scotty" });
    expect(output).toBe("scotty");
  });
  it("should return an empty string if given no branch query", () => {
    const output = getSelectedBranch({});
    expect(output).toBe("");
  });
  // slash escaping is observed to happen before, so it isn't tested here.
});

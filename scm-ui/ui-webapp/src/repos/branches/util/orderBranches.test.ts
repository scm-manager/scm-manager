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

import { orderBranches } from "./orderBranches";

const branch1 = {
  name: "branch1",
  revision: "revision1"
};
const branch2 = {
  name: "branch2",
  revision: "revision2"
};
const branch3 = {
  name: "branch3",
  revision: "revision3",
  defaultBranch: true
};
const defaultBranch = {
  name: "default",
  revision: "revision4",
  defaultBranch: false
};
const developBranch = {
  name: "develop",
  revision: "revision5",
  defaultBranch: false
};
const mainBranch = {
  name: "main",
  revision: "revision6",
  defaultBranch: false
};
const masterBranch = {
  name: "master",
  revision: "revision7",
  defaultBranch: false
};

describe("order branches", () => {
  it("should return branches", () => {
    const branches = [branch1, branch2];
    orderBranches(branches);
    expect(branches).toEqual([branch1, branch2]);
  });

  it("should return defaultBranch first", () => {
    const branches = [branch1, branch2, branch3];
    orderBranches(branches);
    expect(branches).toEqual([branch3, branch1, branch2]);
  });

  it("should order special branches as follows: main > master > default > develop", () => {
    const branches = [defaultBranch, mainBranch, developBranch, masterBranch];
    orderBranches(branches);
    expect(branches).toEqual([mainBranch, masterBranch, defaultBranch, developBranch]);
  });

  it("should order special branches but starting with defaultBranch", () => {
    const branches = [masterBranch, developBranch, defaultBranch, branch3];
    orderBranches(branches);
    expect(branches).toEqual([branch3, masterBranch, defaultBranch, developBranch]);
  });
});

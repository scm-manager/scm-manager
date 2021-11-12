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

import { orderBranches } from "./orderBranches";

const branch1 = {
  name: "branch1",
  revision: "revision1",
};
const branch2 = {
  name: "branch2",
  revision: "revision2",
};
const branch3 = {
  name: "branch3",
  revision: "revision3",
  defaultBranch: true,
};
const defaultBranch = {
  name: "default",
  revision: "revision4",
  defaultBranch: false,
};
const developBranch = {
  name: "develop",
  revision: "revision5",
  defaultBranch: false,
};
const mainBranch = {
  name: "main",
  revision: "revision6",
  defaultBranch: false,
};
const masterBranch = {
  name: "master",
  revision: "revision7",
  defaultBranch: false,
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

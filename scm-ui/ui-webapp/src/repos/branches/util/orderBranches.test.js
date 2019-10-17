import { orderBranches } from "./orderBranches";

const branch1 = { name: "branch1", revision: "revision1" };
const branch2 = { name: "branch2", revision: "revision2" };
const branch3 = { name: "branch3", revision: "revision3", defaultBranch: true };
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
const masterBranch = {
  name: "master",
  revision: "revision6",
  defaultBranch: false
};

describe("order branches", () => {
  it("should return branches", () => {
    let branches = [branch1, branch2];
    orderBranches(branches);
    expect(branches).toEqual([branch1, branch2]);
  });

  it("should return defaultBranch first", () => {
    let branches = [branch1, branch2, branch3];
    orderBranches(branches);
    expect(branches).toEqual([branch3, branch1, branch2]);
  });

  it("should order special branches as follows: master > default > develop", () => {
    let branches = [defaultBranch, developBranch, masterBranch];
    orderBranches(branches);
    expect(branches).toEqual([masterBranch, defaultBranch, developBranch]);
  });

  it("should order special branches but starting with defaultBranch", () => {
    let branches = [masterBranch, developBranch, defaultBranch, branch3];
    orderBranches(branches);
    expect(branches).toEqual([
      branch3,
      masterBranch,
      defaultBranch,
      developBranch
    ]);
  });
});

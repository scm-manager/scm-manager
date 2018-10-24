// @flow

import { findParent } from "./FileTree";

describe("find parent tests", () => {
  it("should return the parent path", () => {
    expect(findParent("src/main/js/")).toBe("src/main");
    expect(findParent("src/main/js")).toBe("src/main");
    expect(findParent("src/main")).toBe("src");
    expect(findParent("src")).toBe("");
  });
});

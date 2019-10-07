// @flow

import { createLink } from "./FileTreeLeaf";
import type { File } from "@scm-manager/ui-types";

describe("create link tests", () => {
  function dir(path: string): File {
    return {
      name: "dir",
      path: path,
      directory: true,
      length: 1,
      revision: "1a",
      _links: {},
      _embedded: {
        children: []
      }
    };
  }

  it("should create link", () => {
    expect(createLink("src", dir("main"))).toBe("src/main/");
    expect(createLink("src", dir("/main"))).toBe("src/main/");
    expect(createLink("src", dir("/main/"))).toBe("src/main/");
  });

  it("should return base url if the directory path is empty", () => {
    expect(createLink("src", dir(""))).toBe("src/");
  });
});

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

import { createRelativeLink, createFolderLink, encodePart } from "./FileLink";
import { createLocation } from "history";
import { File } from "@scm-manager/ui-types";

describe("create relative link tests", () => {
  it("should create relative link", () => {
    expect(createRelativeLink("http://localhost:8081/scm/repo/scmadmin/scm-manager")).toBe(
      "/scm/repo/scmadmin/scm-manager"
    );
    expect(createRelativeLink("ssh://_anonymous@repo.scm-manager.org:1234/repo/public/anonymous-access")).toBe(
      "/repo/public/anonymous-access"
    );
    expect(createRelativeLink("ssh://server.local/project.git")).toBe("/project.git");
  });
});

describe("create folder link tests", () => {
  function dir(path: string): File {
    return {
      name: "dir",
      path: path,
      directory: true,
      length: 1,
      revision: "1a",
      _links: {},
      _embedded: {
        children: [],
      },
    };
  }

  it("should create folder link", () => {
    expect(createFolderLink("src", dir("main"))).toBe("src/main/");
    expect(createFolderLink("src", dir("/main"))).toBe("src/main/");
    expect(createFolderLink("src", dir("/main/"))).toBe("src/main/");
  });

  it("should return base url if the directory path is empty", () => {
    expect(createFolderLink("src", dir(""))).toBe("src/");
  });

  it("should double encode folder names with percent", () => {
    expect(createFolderLink("src", dir("a%20b"))).toBe("src/a%252520b/");
  });
});

describe("link should keep encoded percentages", () => {
  it("history should create a location with encoded pathname", () => {
    // For version 4.x of history we have to double encode uri components with a '%',
    // because of the following issue https://github.com/remix-run/history/issues/505
    // The issue is fixed with 5.x, but react-router-dom seams not to work with 5.x.
    // So we have to stick with 4.x and the double encoding, until react-router-dom uses version 5.x.
    // This test is mainly to remind us to remove the double encoding after update to 5.x.
    const location = createLocation(encodePart("a%20b"));
    expect(location.pathname).toBe("a%2520b");
  });
});

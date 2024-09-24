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

import { createRelativeLink, createFolderLink, encodePart } from "./FileLink";
import { createLocation } from "history";
import { File } from "@scm-manager/ui-types";

describe("create relative link tests", () => {
  it("should create relative link", () => {
    expect(createRelativeLink("http://localhost:8081/scm/repo/scmadmin/scm-manager", "/scm")).toBe(
      "/repo/scmadmin/scm-manager/code/sources/"
    );
    expect(createRelativeLink("ssh://_anonymous@repo.scm-manager.org:1234/repo/public/anonymous-access", "")).toBe(
      "/repo/public/anonymous-access/code/sources/"
    );
    expect(createRelativeLink("ssh://server.local/project.git", "")).toBe("/project.git/code/sources/");
    expect(createRelativeLink("https://localhost:8081/scm/repo/scmadmin/scm-manager", "/scm", "2", "svn")).toBe(
      "/repo/scmadmin/scm-manager/code/sources/2/"
    );
    expect(createRelativeLink("https://localhost:8081/longContext/repo/scmadmin/scm-manager", "/longContext")).toBe(
      "/repo/scmadmin/scm-manager/code/sources/"
    );
  });

  it("with sub directory", () => {
    expect(createRelativeLink("http://localhost:8081/scm/repo/scmadmin/test02/subfolder", "/scm")).toBe(
      "/repo/scmadmin/test02/code/sources/"
    );
    expect(createRelativeLink("https://localhost:8081/scm/repo/scmadmin/test02/subfolder", "/scm", "2")).toBe(
      "/repo/scmadmin/test02/code/sources/2/subfolder/"
    );
    expect(createRelativeLink("http://localhost:8081/scm/repo/scmadmin/test02/dir1/dir2/dir3", "/scm", null, "svn")).toBe(
      "/repo/scmadmin/test02/code/sources/-1/dir1/dir2/dir3/"
    );
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

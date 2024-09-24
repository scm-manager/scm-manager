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

import React from "react";
import { createLocalLink } from "./MarkdownImageRenderer";

describe("createLocalLink tests", () => {
  const revision = "revision";
  const basePath = `/repo/namespace/name/code/sources/${revision}/`;
  const contentLink = "http://localhost:8081/scm/api/v2/repositories/namespace/name/content/{revision}/{path}";
  const currentPath = basePath + "README.md/";
  const link = "image.png";

  it("should return link for internal scm repo link", () => {
    const internalScmLink = "/repo/namespace/name/code/sources/develop/myImg.png";
    expect(createLocalLink(basePath, contentLink, revision, currentPath, internalScmLink)).toBe(internalScmLink);
  });

  it("should return modified contentLink for absolute link", () => {
    expect(createLocalLink(basePath, contentLink, revision, currentPath, "/path/anotherImg.jpg")).toBe(
      "http://localhost:8081/scm/api/v2/repositories/namespace/name/content/revision/path/anotherImg.jpg"
    );
  });

  it("should URI encode branch", () => {
    expect(
      createLocalLink(
        "/repo/namespace/name/code/sources/feature/awesome/",
        contentLink,
        "feature/awesome",
        currentPath,
        link
      )
    ).toContain("feature%2Fawesome");
  });
});

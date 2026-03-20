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

import { Repository } from "@scm-manager/ui-types";
import { createLocalLink } from "./MarkdownLinkRenderer";

describe("MarkdownLinkRenderer createLocalLink tests", () => {
  const basePath = "/repo/ns/name/code/sources/";
  const repository: Repository = { _links: {}, name: "name", namespace: "ns", type: "" };
  const revision = "main";
  const currentPath = "/repo/ns/name/code/sources/main/folder/README.md/";

  it("should return link unchanged for internal scm repo links", () => {
    const internalScmLink = "/repo/ns/name/code/changeset/12345";
    expect(createLocalLink(repository, revision, currentPath, internalScmLink)).toBe(internalScmLink);
  });

  it("should return normalized absolute path starting with slash", () => {
    const absoluteLink = "/docs/CONTRIBUTE.md";
    expect(createLocalLink(repository, revision, currentPath, absoluteLink)).toBe(
      basePath + revision + "/docs/CONTRIBUTE.md"
    );
  });

  it("should handle absolute links with redundant segments", () => {
    const messyAbsoluteLink = "//docs/./CONTRIBUTE.md";
    expect(createLocalLink(repository, revision, currentPath, messyAbsoluteLink)).toBe(
      basePath + revision + "/docs/CONTRIBUTE.md"
    );
  });

  it("should return internal link starting with slash for relative paths", () => {
    const relativeLink = "img/image.png";
    const result = createLocalLink(repository, revision, currentPath, relativeLink);
    expect(result).toBe(basePath + revision + "/folder/img/image.png");
  });

  it("should handle relative links navigating up with ..", () => {
    const parentLink = "../docs/CHANGELOG.md";
    const result = createLocalLink(repository, revision, currentPath, parentLink);
    expect(result).toBe(basePath + revision + "/docs/CHANGELOG.md");
  });
});

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

import { createLocalLink } from "./MarkdownImageRenderer";

describe("MarkdownImageRenderer createLocalLink tests", () => {
  const contentLinkBase = "http://localhost:8081/scm/api/v2/repositories/ns/name/content/";
  const contentLink = contentLinkBase + "{revision}/{path}";
  const revision = "main";
  const currentPath = "/repo/ns/name/code/sources/main/folder/README.md/";

  it("should return link unchanged for internal scm repo links", () => {
    const internalScmLink = "/repo/ns/name/code/sources/develop/myImg.png";
    expect(createLocalLink(contentLink, revision, currentPath, internalScmLink)).toBe(internalScmLink);
  });

  it("should return normalized absolute path starting with slash", () => {
    const absoluteLink = "/path/anotherImg.jpg";
    expect(createLocalLink(contentLink, revision, currentPath, absoluteLink)).toBe(
      `${contentLinkBase}${revision}${absoluteLink}`
    );
  });

  it("should URI encode revision", () => {
    expect(createLocalLink(contentLink, "feature/awesome", currentPath, "image.png")).toContain("feature%2Fawesome");
  });

  it("should inject the resolved path into {path} placeholder", () => {
    const link = "image.png";
    const result = createLocalLink(contentLink, revision, currentPath, link);
    expect(result).toBe(`${contentLinkBase}${revision}/folder/image.png`);
  });
});

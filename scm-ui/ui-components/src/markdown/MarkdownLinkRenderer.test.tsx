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

import { isAnchorLink, isExternalLink, isLinkWithProtocol, isInternalScmRepoLink } from "./paths";
import { createLocalLink } from "./MarkdownLinkRenderer";

describe("test isAnchorLink", () => {
  it("should return true", () => {
    expect(isAnchorLink("#some-thing")).toBe(true);
    expect(isAnchorLink("#/some/more/complicated-link")).toBe(true);
  });

  it("should return false", () => {
    expect(isAnchorLink("https://cloudogu.com")).toBe(false);
    expect(isAnchorLink("/some/path/link")).toBe(false);
  });
});

describe("test isExternalLink", () => {
  it("should return true", () => {
    expect(isExternalLink("https://cloudogu.com")).toBe(true);
    expect(isExternalLink("http://cloudogu.com")).toBe(true);
  });

  it("should return false", () => {
    expect(isExternalLink("some/path/link")).toBe(false);
    expect(isExternalLink("/some/path/link")).toBe(false);
    expect(isExternalLink("#some-anchor")).toBe(false);
    expect(isExternalLink("mailto:trillian@hitchhiker.com")).toBe(false);
  });
});

describe("test isLinkWithProtocol", () => {
  it("should return true", () => {
    expect(isLinkWithProtocol("ldap://[2001:db8::7]/c=GB?objectClass?one")).toBeTruthy();
    expect(isLinkWithProtocol("mailto:trillian@hitchhiker.com")).toBeTruthy();
    expect(isLinkWithProtocol("tel:+1-816-555-1212")).toBeTruthy();
    expect(isLinkWithProtocol("urn:oasis:names:specification:docbook:dtd:xml:4.1.2")).toBeTruthy();
    expect(isLinkWithProtocol("about:config")).toBeTruthy();
    expect(isLinkWithProtocol("http://cloudogu.com")).toBeTruthy();
    expect(isLinkWithProtocol("file:///srv/git/project.git")).toBeTruthy();
    expect(isLinkWithProtocol("ssh://trillian@server/project.git")).toBeTruthy();
  });
  it("should return false", () => {
    expect(isLinkWithProtocol("some/path/link")).toBeFalsy();
    expect(isLinkWithProtocol("/some/path/link")).toBeFalsy();
    expect(isLinkWithProtocol("#some-anchor")).toBeFalsy();
  });
});

describe("test isInternalScmRepoLink", () => {
  it("should return true", () => {
    expect(isInternalScmRepoLink("/repo/scmadmin/git/code/changeset/1234567")).toBe(true);
    expect(isInternalScmRepoLink("/repo/scmadmin/git")).toBe(true);
  });
  it("should return false", () => {
    expect(isInternalScmRepoLink("repo/path/link")).toBe(false);
    expect(isInternalScmRepoLink("/some/path/link")).toBe(false);
    expect(isInternalScmRepoLink("#some-anchor")).toBe(false);
  });
});

describe("test createLocalLink", () => {
  it("should handle relative links", () => {
    expectLocalLink("/src", "/src/README.md", "docs/Home.md", "/src/docs/Home.md");
  });

  it("should handle absolute links", () => {
    expectLocalLink("/src", "/src/README.md", "/docs/CHANGELOG.md", "/src/docs/CHANGELOG.md");
  });

  it("should handle relative links from locations with trailing slash", () => {
    expectLocalLink("/src", "/src/README.md/", "/docs/LICENSE.md", "/src/docs/LICENSE.md");
  });

  it("should handle relative links from location outside of base", () => {
    expectLocalLink("/src", "/info/readme", "docs/index.md", "/src/docs/index.md");
  });

  it("should handle absolute links from location outside of base", () => {
    expectLocalLink("/src", "/info/readme", "/info/index.md", "/src/info/index.md");
  });

  it("should handle relative links from sub directories", () => {
    expectLocalLink("/src", "/src/docs/index.md", "installation/linux.md", "/src/docs/installation/linux.md");
  });

  it("should handle absolute links from sub directories", () => {
    expectLocalLink("/src", "/src/docs/index.md", "/docs/CONTRIBUTIONS.md", "/src/docs/CONTRIBUTIONS.md");
  });

  it("should resolve .. with in path", () => {
    expectLocalLink("/src", "/src/docs/installation/index.md", "../../README.md", "/src/README.md");
  });

  it("should resolve .. to / if we reached the end", () => {
    expectLocalLink("/", "/index.md", "../../README.md", "/README.md");
  });

  it("should resolve . with in path", () => {
    expectLocalLink("/src", "/src/README.md", "./SHAPESHIPS.md", "/src/SHAPESHIPS.md");
  });

  it("should resolve . with the current directory", () => {
    expectLocalLink("/", "/README.md", "././HITCHHIKER.md", "/HITCHHIKER.md");
  });

  it("should handle complex path", () => {
    expectLocalLink("/src", "/src/docs/installation/index.md", "./.././../docs/index.md", "/src/docs/index.md");
  });

  const expectLocalLink = (basePath: string, currentPath: string, link: string, expected: string) => {
    const localLink = createLocalLink(basePath, currentPath, link);
    expect(localLink).toBe(expected);
  };
});

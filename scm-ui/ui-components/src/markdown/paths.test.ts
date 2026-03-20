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

import { isAnchorLink, isExternalLink, isLinkWithProtocol, isInternalScmRepoLink, resolveInternalPath } from "./paths";

describe("isExternalLink tests", () => {
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

describe("isAnchorLink tests", () => {
  it("should return true", () => {
    expect(isAnchorLink("#some-thing")).toBe(true);
    expect(isAnchorLink("#/some/more/complicated-link")).toBe(true);
  });

  it("should return false", () => {
    expect(isAnchorLink("https://cloudogu.com")).toBe(false);
    expect(isAnchorLink("/some/path/link")).toBe(false);
  });
});

describe("isInternalScmRepoLink tests", () => {
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

describe("isLinkWithProtocol tests", () => {
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

describe("resolveInternalPath tests", () => {
  const revision = "main";
  const repoPath = "/repo/ns/name/code/sources/main/";

  it("should resolve . within the path", () => {
    const currentPath = repoPath + "README.md";
    const link = "./SHAPESHIPS.md";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("SHAPESHIPS.md");
  });

  it("should resolve . with the current directory", () => {
    const currentPath = repoPath + "README.md";
    const link = "././HITCHHIKER.md";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("HITCHHIKER.md");
  });

  it("should resolve .. within the path", () => {
    const currentPath = repoPath + "docs/gui/index.md";
    const link = "../../img/image.png";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("img/image.png");
  });

  it("should handle complex redundant segments (./.././..)", () => {
    const currentPath = repoPath + "docs/installation/index.md";
    const link = "./.././../docs/index.md";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("docs/index.md");
  });

  it("should resolve .. to root if we reach the end", () => {
    const currentPath = repoPath + "index.md";
    const link = "../../README.md";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("README.md");
  });

  it("should resolve root link within root path", () => {
    const currentPath = repoPath + "README.md";
    const link = "/SHAPESHIPS.md";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("SHAPESHIPS.md");
  });

  it("should resolve root link within folder path", () => {
    const currentPath = repoPath + "dir/README.md";
    const link = "/SHAPESHIPS.md";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("SHAPESHIPS.md");
  });

  it("should resolve relative link within root path", () => {
    const currentPath = repoPath + "README.md";
    const link = "SHAPESHIPS.md";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("SHAPESHIPS.md");
  });

  it("should resolve relative link within folder path", () => {
    const currentPath = repoPath + "dir/README.md";
    const link = "SHAPESHIPS.md";
    const result = resolveInternalPath(currentPath, revision, link);
    expect(result).toBe("dir/SHAPESHIPS.md");
  });
});

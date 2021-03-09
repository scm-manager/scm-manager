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
import {
  isAnchorLink,
  isExternalLink,
  isLinkWithProtocol,
  createLocalLink,
  isInternalScmRepoLink
} from "./MarkdownLinkRenderer";

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
    expect(isLinkWithProtocol("ldap://[2001:db8::7]/c=GB?objectClass?one")).toBe(true);
    expect(isLinkWithProtocol("mailto:trillian@hitchhiker.com")).toBe(true);
    expect(isLinkWithProtocol("tel:+1-816-555-1212")).toBe(true);
    expect(isLinkWithProtocol("urn:oasis:names:specification:docbook:dtd:xml:4.1.2")).toBe(true);
    expect(isLinkWithProtocol("about:config")).toBe(true);
    expect(isLinkWithProtocol("http://cloudogu.com")).toBe(true);
    expect(isLinkWithProtocol("file:///srv/git/project.git")).toBe(true);
    expect(isLinkWithProtocol("ssh://trillian@server/project.git")).toBe(true);
  });
  it("should return false", () => {
    expect(isLinkWithProtocol("some/path/link")).toBe(false);
    expect(isLinkWithProtocol("/some/path/link")).toBe(false);
    expect(isLinkWithProtocol("#some-anchor")).toBe(false);
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

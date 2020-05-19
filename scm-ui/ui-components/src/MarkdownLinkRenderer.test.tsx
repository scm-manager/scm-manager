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
import { isAnchorLink, isExternalLink, isLinkWithProtocol, createLocalLink } from "./MarkdownLinkRenderer";

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
  });
  it("should return false", () => {
    expect(isExternalLink("some/path/link")).toBe(false);
    expect(isExternalLink("/some/path/link")).toBe(false);
    expect(isExternalLink("#some-anchor")).toBe(false);
  });
});

describe("test createLocalLink", () => {
  const basePath = "/repo/space/name/sources/master/";
  const pathname = basePath + "README.md/";

  it("should return same directory", () => {
    expect(createLocalLink(pathname, "./another.md")).toBe(basePath + "./another.md/");
    expect(createLocalLink(pathname, "./another.md#42")).toBe(basePath + "./another.md/#42");
  });

  it("should return main directory", () => {
    expect(createLocalLink(pathname, "/")).toBe("/");
    expect(createLocalLink(pathname, "/users/")).toBe("/users/");
    expect(createLocalLink(pathname, "/users/#42")).toBe("/users/#42");
  });

  it("should return ascend directory", () => {
    expect(createLocalLink(pathname, "../")).toBe(basePath + "../");
    expect(createLocalLink(pathname, "../../")).toBe(basePath + "../../");
  });

  it("should return deeper links", () => {
    expect(createLocalLink(pathname, "docs/Home.md")).toBe(basePath + "docs/Home.md/");
    expect(createLocalLink(pathname, "docs/Home.md#42")).toBe(basePath + "docs/Home.md/#42");
  });
});

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
import React from "react";
import { correctLocalLink } from "./MarkdownLinkRenderer";

const basePath = "/repo/space/name/sources/master/";
const pathname = basePath + "README.md/";

describe("correctLocalLink tests", () => {
  it("should return same url", () => {
    expect(correctLocalLink(pathname, "")).toBe(pathname);
  });

  it("should return main directory", () => {
    expect(correctLocalLink(pathname, "/")).toBe("/");
    expect(correctLocalLink(pathname, "/users/")).toBe("/users/");
    expect(correctLocalLink(pathname, "/users/#42")).toBe("/users/#42");
  });

  it("should return ascend directory", () => {
    expect(correctLocalLink(pathname, "../")).toBe(basePath + "../"); // one layer up
    expect(correctLocalLink(pathname, "../../")).toBe(basePath + "../../"); // one layer up
  });

  it("should return same directory", () => {
    expect(correctLocalLink(pathname, "./another.md")).toBe(basePath + "./another.md/"); // same layer
    expect(correctLocalLink(pathname, "./another.md#42")).toBe(basePath + "./another.md/#42"); // same layer
  });

  it("should return deeper links", () => {
    expect(correctLocalLink(pathname, "docs/Home.md")).toBe(basePath + "docs/Home.md/");
    expect(correctLocalLink(pathname, "docs/Home.md#42")).toBe(basePath + "docs/Home.md/#42");
  });

  it("should return external link", () => {
    expect(correctLocalLink(pathname, "https://foo.bar/baz#42")).toBe("https://foo.bar/baz#42");
    expect(correctLocalLink(pathname, "ldap://[2001:db8::7]/c=GB?objectClass?one")).toBe(
      "ldap://[2001:db8::7]/c=GB?objectClass?one"
    );
    expect(correctLocalLink(pathname, "mailto:John.Doe@example.com")).toBe("mailto:John.Doe@example.com");
    expect(correctLocalLink(pathname, "http://userid:password@example.com:8080")).toBe(
      "http://userid:password@example.com:8080"
    );
    expect(correctLocalLink(pathname, "tel:+1-816-555-1212")).toBe("tel:+1-816-555-1212");
    expect(correctLocalLink(pathname, "urn:oasis:names:specification:docbook:dtd:xml:4.1.2")).toBe(
      "urn:oasis:names:specification:docbook:dtd:xml:4.1.2"
    );
    expect(correctLocalLink(pathname, "about:config")).toBe("about:config");
  });
});

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

import { loginRedirectTarget } from "./loginRedirect";
import { from } from "./Login";

// query-string of a login redirect target, e.g. "?from=%2Fadmin%2Finfo"
const searchOf = (target: string) => target.substring(target.indexOf("?"));

describe("loginRedirectTarget", () => {
  it("preserves the requested location as an app-relative from, stripping the context path", () => {
    const target = loginRedirectTarget("https://scm.example.com/scm/admin/info", "/scm");
    expect(target).toBe("/login?from=%2Fadmin%2Finfo");
  });

  it("works without a context path", () => {
    const target = loginRedirectTarget("https://scm.example.com/admin/info", "");
    expect(target).toBe("/login?from=%2Fadmin%2Finfo");
  });

  it("keeps the query string of the requested location", () => {
    const target = loginRedirectTarget("https://scm.example.com/scm/repos/ns/name?branch=x", "/scm");
    expect(target).toBe("/login?from=%2Frepos%2Fns%2Fname%3Fbranch%3Dx");
  });

  it("does not add a from for the root location", () => {
    expect(loginRedirectTarget("https://scm.example.com/scm/", "/scm")).toBe("/login");
    expect(loginRedirectTarget("https://scm.example.com/scm", "/scm")).toBe("/login");
  });

  it("round-trips through Login's from() back to the original app-relative path", () => {
    const target = loginRedirectTarget("https://scm.example.com/scm/admin/info", "/scm");
    expect(from(searchOf(target))).toBe("/admin/info");
  });

  it("round-trips a location with query string through from()", () => {
    const target = loginRedirectTarget("https://scm.example.com/scm/repos/ns/name?branch=x", "/scm");
    expect(from(searchOf(target))).toBe("/repos/ns/name?branch=x");
  });
});

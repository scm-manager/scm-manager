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
import { renderHook } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import useActiveMatch from "./useActiveMatch";
import { ReactNode } from "react";
import { RoutingProps } from "./RoutingProps";

describe("useActiveMatch tests", () => {
  const setup = (currentUrl: string, props: RoutingProps) => {
    return renderHook(() => useActiveMatch(props), {
      wrapper: ({ children }: { children: ReactNode }) => (
        <MemoryRouter initialEntries={[currentUrl]}>{children}</MemoryRouter>
      ),
    });
  };

  it("should match with same route", () => {
    const { result } = setup("/scm/repos", { to: "/scm/repos" });
    expect(result.current).toBe(true);
  });

  it("should not match with different routes", () => {
    const { result } = setup("/scm/repos", { to: "/scm/admin" });
    expect(result.current).toBe(false);
  });

  describe("Nested routes", () => {
    it("should match nested route", () => {
      const { result } = setup("/scm/repo/123", { to: "/scm/repo" });
      expect(result.current).toBe(true);
    });

    it("should not match different nested routes", () => {
      const { result } = setup("/scm/repo/123/123/code/sources", { to: "/scm/repo/123/123/info" });
      expect(result.current).toBe(false);
    });

    it("should match parent route", () => {
      const { result } = setup("/scm/admin/settings/general", { to: "/scm/admin/settings" });
      expect(result.current).toBe(true);
    });
  });

  describe("Exact flag", () => {
    it("should not match with exact flag", () => {
      const { result } = setup("/scm/admin/settings/general", {
        to: "/scm/admin/settings",
        matchWhenExact: true,
      });
      expect(result.current).toBe(false);
    });

    it("should match same route with flag", () => {
      const { result } = setup("/scm/admin/settings/general", {
        to: "/scm/admin/settings/general",
        matchWhenExact: true,
      });
      expect(result.current).toBe(true);
    });
  });

  describe("ActiveWhenMatch via Regex", () => {
    it("should match with valid regex", () => {
      const { result } = setup("/scm/repos/repo-name/branches", {
        to: "/scm/repos",
        activeWhenMatch: "^/scm/repos/.*/branches$",
      });
      expect(result.current).toBe(true);
    });

    it("should not match with wrong regex", () => {
      const { result } = setup("/scm/repo/repo-name/branches", {
        to: "/anywhere",
        activeWhenMatch: "^/scm/repos/.*/branches$",
      });
      expect(result.current).toBe(false);
    });

    it("should match different route with regex", () => {
      const { result } = setup("/scm/repo/123", {
        to: "/scm/repos",
        activeWhenMatch: "^/scm/repo",
      });
      expect(result.current).toBe(true);
    });

    it("should match different route with regex object", () => {
      const { result } = setup("/scm/repo/123/settings", {
        to: "/scm/repos",
        activeWhenMatch: /^\/scm\/repo/,
      });
      expect(result.current).toBe(true);
    });
  });

  it("should not match partially matching route names (prefix collision)", () => {
    const { result } = setup("/scm/admin_panel/settings", { to: "/scm/admin" });
    expect(result.current).toBe(false);
  });

  it("should match when current url contains query parameters", () => {
    const { result } = setup("/scm/repos?sort=desc&page=2", { to: "/scm/repos" });
    expect(result.current).toBe(true);
  });

  it("should not match if the 'to' link contains a hash", () => {
    const { result } = setup("/scm/repos", { to: "/scm/repos#section2" });
    expect(result.current).toBe(false);
  });

  it("should match when current url has a trailing slash", () => {
    const { result } = setup("/scm/repos/", { to: "/scm/repos" });
    expect(result.current).toBe(true);
  });

  describe("URL encoded paths", () => {
    it("should match parent route when current url contains encoded slashes", () => {
      const { result } = setup("/scm/repo/bugfix%2Faccessibility/settings", {
        to: "/scm/repo",
      });
      expect(result.current).toBe(true);
    });

    it("should match exact route with encoded characters", () => {
      const { result } = setup("/scm/repo/bugfix%2Faccessibility/settings", {
        to: "/scm/repo/bugfix%2Faccessibility",
      });
      expect(result.current).toBe(true);
    });

    it("should match via regex when url contains encoded characters", () => {
      const { result } = setup("/scm/repo/bugfix%2Faccessibility/settings", {
        to: "/anywhere",
        activeWhenMatch: "^/scm/repo/.*%2F.*/settings$",
      });
      expect(result.current).toBe(true);
    });
  });
});

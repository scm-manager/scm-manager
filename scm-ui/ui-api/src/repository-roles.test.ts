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

import { RepositoryRole, RepositoryRoleCollection } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock-jest";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import { act } from "react-test-renderer";
import {
  useCreateRepositoryRole,
  useDeleteRepositoryRole,
  useRepositoryRole, useRepositoryRoles,
  useUpdateRepositoryRole
} from "./repository-roles";

describe("Test repository-roles hooks", () => {
  const roleName = "theroleingstones";
  const role: RepositoryRole = {
    name: roleName,
    verbs: ["rocking"],
    _links: {
      delete: {
        href: "/repositoryRoles/theroleingstones"
      },
      update: {
        href: "/repositoryRoles/theroleingstones"
      }
    }
  };

  const roleCollection: RepositoryRoleCollection = {
    page: 0,
    pageTotal: 0,
    _links: {},
    _embedded: {
      repositoryRoles: [role]
    }
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useRepositoryRoles tests", () => {
    it("should return repositoryRoles", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");
      fetchMock.get("/api/v2/repositoryRoles", roleCollection);
      const { result, waitFor } = renderHook(() => useRepositoryRoles(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(roleCollection);
    });

    it("should return paged repositoryRoles", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");
      fetchMock.get("/api/v2/repositoryRoles", roleCollection, {
        query: {
          page: "42"
        }
      });
      const { result, waitFor } = renderHook(() => useRepositoryRoles({ page: 42 }), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(roleCollection);
    });

    it("should update repositoryRole cache", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");
      fetchMock.get("/api/v2/repositoryRoles", roleCollection);
      const { result, waitFor } = renderHook(() => useRepositoryRoles(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => !!result.current.data);
      expect(queryClient.getQueryData(["repositoryRole", roleName])).toEqual(role);
    });
  });

  describe("useRepositoryRole tests", () => {
    it("should return repositoryRole", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");
      fetchMock.get("/api/v2/repositoryRoles/" + roleName, role);
      const { result, waitFor } = renderHook(() => useRepositoryRole(roleName), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(role);
    });
  });

  describe("useCreateRepositoryRole tests", () => {
    it("should create repositoryRole", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");

      fetchMock.postOnce("/api/v2/repositoryRoles", {
        status: 201,
        headers: {
          Location: "/repositoryRoles/" + roleName
        }
      });

      fetchMock.getOnce("/api/v2/repositoryRoles/" + roleName, role);

      const { result, waitForNextUpdate } = renderHook(() => useCreateRepositoryRole(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { create } = result.current;
        create(role);
        return waitForNextUpdate();
      });

      expect(result.current.repositoryRole).toEqual(role);
    });

    it("should fail without location header", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");

      fetchMock.postOnce("/api/v2/repositoryRoles", {
        status: 201
      });

      fetchMock.getOnce("/api/v2/repositoryRoles/" + roleName, role);

      const { result, waitForNextUpdate } = renderHook(() => useCreateRepositoryRole(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { create } = result.current;
        create(role);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeDefined();
    });
  });

  describe("useDeleteRepositoryRole tests", () => {
    it("should delete repositoryRole", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");

      fetchMock.deleteOnce("/api/v2/repositoryRoles/" + roleName, {
        status: 200
      });

      const { result, waitForNextUpdate } = renderHook(() => useDeleteRepositoryRole(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { remove } = result.current;
        remove(role);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isDeleted).toBe(true);
      expect(result.current.isLoading).toBe(false);
      expect(queryClient.getQueryData(["repositoryRole", roleName])).toBeUndefined();
    });
  });

  describe("useUpdateRepositoryRole tests", () => {
    it("should update repositoryRole", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");

      const newRole: RepositoryRole = {
        ...role,
        name: "newname"
      };

      fetchMock.putOnce("/api/v2/repositoryRoles/" + roleName, {
        status: 200
      });

      const { result, waitForNextUpdate } = renderHook(() => useUpdateRepositoryRole(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { update } = result.current;
        update(newRole);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isUpdated).toBe(true);
      expect(result.current.isLoading).toBe(false);
      expect(queryClient.getQueryData(["repositoryRole", roleName])).toBeUndefined();
      expect(queryClient.getQueryData(["repositoryRole", "newname"])).toBeUndefined();
      expect(queryClient.getQueryData(["repositoryRoles"])).toBeUndefined();
    });
  });
});

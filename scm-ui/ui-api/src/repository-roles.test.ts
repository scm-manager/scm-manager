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

import { RepositoryRole, RepositoryRoleCollection } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import { renderHook, act, waitFor } from "@testing-library/react";
import createWrapper from "./tests/createWrapper";
import {
  useCreateRepositoryRole,
  useDeleteRepositoryRole,
  useRepositoryRole,
  useRepositoryRoles,
  useUpdateRepositoryRole,
} from "./repository-roles";

describe("Test repository-roles hooks", () => {
  const roleName = "theroleingstones";
  const role: RepositoryRole = {
    name: roleName,
    verbs: ["rocking"],
    _links: {
      delete: {
        href: "/repositoryRoles/theroleingstones",
      },
      update: {
        href: "/repositoryRoles/theroleingstones",
      },
    },
  };

  const roleCollection: RepositoryRoleCollection = {
    page: 0,
    pageTotal: 0,
    _links: {},
    _embedded: {
      repositoryRoles: [role],
    },
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useRepositoryRoles tests", () => {
    it("should return repositoryRoles", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");
      fetchMock.get("/api/v2/repositoryRoles", roleCollection);
      const { result } = renderHook(() => useRepositoryRoles(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(roleCollection));
    });

    it("should return paged repositoryRoles", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");
      fetchMock.get("/api/v2/repositoryRoles", roleCollection, {
        query: {
          page: "42",
        },
      });
      const { result } = renderHook(() => useRepositoryRoles({ page: 42 }), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(roleCollection));
    });

    it("should update repositoryRole cache", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");
      fetchMock.get("/api/v2/repositoryRoles", roleCollection);
      renderHook(() => useRepositoryRoles(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(queryClient.getQueryData(["repositoryRole", roleName])).toEqual(role));
    });
  });

  describe("useRepositoryRole tests", () => {
    it("should return repositoryRole", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");
      fetchMock.get("/api/v2/repositoryRoles/" + roleName, role);
      const { result } = renderHook(() => useRepositoryRole(roleName), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(role));
    });
  });

  describe("useCreateRepositoryRole tests", () => {
    it("should create repositoryRole", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");

      fetchMock.postOnce("/api/v2/repositoryRoles", {
        status: 201,
        headers: {
          Location: "/repositoryRoles/" + roleName,
        },
      });

      fetchMock.getOnce("/api/v2/repositoryRoles/" + roleName, role);

      const { result } = renderHook(() => useCreateRepositoryRole(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.create(role);
      });

      await waitFor(() => {
        expect(result.current.repositoryRole).toEqual(role);
      });
    });

    it("should fail without location header", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");

      fetchMock.postOnce("/api/v2/repositoryRoles", {
        status: 201,
      });

      fetchMock.getOnce("/api/v2/repositoryRoles/" + roleName, role);

      const { result } = renderHook(() => useCreateRepositoryRole(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.create(role);
      });

      expect(result.current.error).toBeDefined();
    });
  });

  describe("useDeleteRepositoryRole tests", () => {
    it("should delete repositoryRole", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");

      fetchMock.deleteOnce("/api/v2/repositoryRoles/" + roleName, {
        status: 200,
      });

      const { result } = renderHook(() => useDeleteRepositoryRole(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.remove(role);
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isDeleted).toBe(true);
      expect(result.current.isPending).toBe(false);
      expect(queryClient.getQueryData(["repositoryRole", roleName])).toBeUndefined();
    });
  });

  describe("useUpdateRepositoryRole tests", () => {
    it("should update repositoryRole", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryRoles", "/repositoryRoles");

      fetchMock.putOnce("/api/v2/repositoryRoles/" + roleName, {
        status: 200,
      });

      const { result } = renderHook(() => useUpdateRepositoryRole(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.update(role);
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isUpdated).toBe(true);
      expect(result.current.isPending).toBe(false);
      expect(queryClient.getQueryData(["repositoryRole", roleName])).toBeUndefined();
      expect(queryClient.getQueryData(["repositoryRole", "newname"])).toBeUndefined();
      expect(queryClient.getQueryData(["repositoryRoles"])).toBeUndefined();
    });
  });
});

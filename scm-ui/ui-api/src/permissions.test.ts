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

import { setIndexLink } from "./tests/indexLinks";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import {
  Namespace,
  Permission,
  PermissionCollection,
  Repository,
  RepositoryRole,
  RepositoryRoleCollection,
  RepositoryVerbs,
} from "@scm-manager/ui-types";
import fetchMock from "fetch-mock";
import { renderHook, act, waitFor } from "@testing-library/react";
import createWrapper from "./tests/createWrapper";
import {
  useAvailablePermissions,
  useCreatePermission,
  useDeletePermission,
  usePermissions,
  useRepositoryVerbs,
  useUpdatePermission,
} from "./permissions";

describe("permission hooks test", () => {
  const readRole: RepositoryRole = {
    name: "READ",
    verbs: ["read", "pull"],
    _links: {},
  };

  const roleCollection: RepositoryRoleCollection = {
    _embedded: {
      repositoryRoles: [readRole],
    },
    _links: {},
    page: 1,
    pageTotal: 1,
  };

  const verbCollection: RepositoryVerbs = {
    verbs: ["read", "pull"],
    _links: {},
  };

  const readPermission: Permission = {
    name: "trillian",
    role: "READ",
    verbs: [],
    groupPermission: false,
    _links: {
      update: {
        href: "/p/trillian",
      },
    },
  };

  const writePermission: Permission = {
    name: "dent",
    role: "WRITE",
    verbs: [],
    groupPermission: false,
    _links: {
      delete: {
        href: "/p/dent",
      },
    },
  };

  const permissionsRead: PermissionCollection = {
    _embedded: {
      permissions: [readPermission],
    },
    _links: {},
  };

  const permissionsWrite: PermissionCollection = {
    _embedded: {
      permissions: [writePermission],
    },
    _links: {},
  };

  const namespace: Namespace = {
    namespace: "spaceships",
    _links: {
      permissions: {
        href: "/ns/spaceships/permissions",
      },
    },
  };

  const repository: Repository = {
    namespace: "spaceships",
    name: "heart-of-gold",
    type: "git",
    _links: {
      permissions: {
        href: "/r/heart-of-gold/permissions",
      },
    },
  };

  const queryClient = createInfiniteCachingClient();

  beforeEach(() => {
    queryClient.clear();
    fetchMock.reset();
  });

  describe("useRepositoryVerbs tests", () => {
    it("should return available verbs", async () => {
      setIndexLink(queryClient, "repositoryVerbs", "/verbs");
      fetchMock.get("/api/v2/verbs", verbCollection);

      const { result } = renderHook(() => useRepositoryVerbs(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => {
        expect(result.current.data).toEqual(verbCollection);
      });
    });
  });

  describe("useAvailablePermissions tests", () => {
    it("should return available roles and verbs", async () => {
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
        _links: {
          repositoryRoles: {
            href: "/roles",
          },
          repositoryVerbs: {
            href: "/verbs",
          },
        },
      });
      fetchMock.get("/api/v2/roles", roleCollection);
      fetchMock.get("/api/v2/verbs", verbCollection);

      const { result } = renderHook(() => useAvailablePermissions(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => {
        expect(result.current.data?.repositoryRoles).toEqual(roleCollection._embedded?.repositoryRoles);
        expect(result.current.data?.repositoryVerbs).toEqual(verbCollection.verbs);
      });
    });
  });

  describe("usePermissions tests", () => {
    const fetchPermissions = async (namespaceOrRepository: Namespace | Repository) => {
      const { result } = renderHook(() => usePermissions(namespaceOrRepository), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => {
        expect(result.current.data).toBeDefined();
      });

      return result.current.data;
    };

    it("should return permissions from namespace", async () => {
      fetchMock.getOnce("/api/v2/ns/spaceships/permissions", permissionsRead);
      const data = await fetchPermissions(namespace);
      expect(data).toEqual(permissionsRead);
    });

    it("should cache permissions for namespace", async () => {
      fetchMock.getOnce("/api/v2/ns/spaceships/permissions", permissionsRead);
      await fetchPermissions(namespace);
      const data = queryClient.getQueryData(["namespace", "spaceships", "permissions"]);
      expect(data).toEqual(permissionsRead);
    });

    it("should return permissions from repository", async () => {
      fetchMock.getOnce("/api/v2/r/heart-of-gold/permissions", permissionsWrite);
      const data = await fetchPermissions(repository);
      expect(data).toEqual(permissionsWrite);
    });

    it("should cache permissions for repository", async () => {
      fetchMock.getOnce("/api/v2/r/heart-of-gold/permissions", permissionsWrite);
      await fetchPermissions(repository);
      const data = queryClient.getQueryData(["repository", "spaceships", "heart-of-gold", "permissions"]);
      expect(data).toEqual(permissionsWrite);
    });
  });

  describe("useCreatePermission tests", () => {
    const createAndFetch = async () => {
      fetchMock.postOnce("/api/v2/ns/spaceships/permissions", {
        status: 201,
        headers: {
          Location: "/ns/spaceships/permissions/42",
        },
      });

      fetchMock.getOnce("/api/v2/ns/spaceships/permissions/42", readPermission);

      const { result } = renderHook(() => useCreatePermission(namespace), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.create(readPermission);
      });

      await waitFor(() => {
        expect(result.current.permission).toEqual(readPermission);
      });

      return result.current;
    };

    it("should create permission", async () => {
      const data = await createAndFetch();
      expect(data.permission).toEqual(readPermission);
    });

    it("should fail without location header", async () => {
      fetchMock.postOnce("/api/v2/ns/spaceships/permissions", {
        status: 201,
      });

      const { result } = renderHook(() => useCreatePermission(namespace), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.create(readPermission);
      });

      await waitFor(() => {
        expect(result.current.error).toBeDefined();
      });
    });

    it("should invalidate namespace cache", async () => {
      const key = ["namespace", "spaceships", "permissions"];
      queryClient.setQueryData(key, permissionsRead);
      await createAndFetch();

      const state = queryClient.getQueryState(key);
      expect(state?.isInvalidated).toBe(true);
    });
  });

  describe("useDeletePermission tests", () => {
    const deletePermission = async () => {
      fetchMock.deleteOnce("/api/v2/p/dent", {
        status: 204,
      });

      const { result } = renderHook(() => useDeletePermission(repository), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.remove(writePermission);
      });

      await waitFor(() => {
        expect(result.current.isDeleted).toBe(true);
      });

      return result.current;
    };

    const shouldInvalidateQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await deletePermission();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState?.isInvalidated).toBe(true);
    };

    it("should delete permission", async () => {
      const { isDeleted } = await deletePermission();

      expect(isDeleted).toBe(true);
    });

    it("should invalidate permission cache", async () => {
      await shouldInvalidateQuery(["repository", "spaceships", "heart-of-gold", "permissions"], permissionsWrite);
    });
  });

  describe("useUpdatePermission tests", () => {
    const updatePermission = async () => {
      fetchMock.putOnce("/api/v2/p/trillian", {
        status: 204,
      });

      const { result } = renderHook(() => useUpdatePermission(repository), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.update(readPermission);
      });

      await waitFor(() => {
        expect(result.current.isUpdated).toBe(true);
      });

      return result.current;
    };

    const shouldInvalidateQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await updatePermission();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState?.isInvalidated).toBe(true);
    };

    it("should update permission", async () => {
      const { isUpdated } = await updatePermission();

      expect(isUpdated).toBe(true);
    });

    it("should invalidate permission cache", async () => {
      await shouldInvalidateQuery(["repository", "spaceships", "heart-of-gold", "permissions"], permissionsRead);
    });
  });
});

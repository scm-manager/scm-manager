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

import { Group } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock-jest";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import { renderHook } from "@testing-library/react-hooks";
import { useCreateGroup, useDeleteGroup, useGroup, useGroups, useUpdateGroup } from "./groups";
import createWrapper from "./tests/createWrapper";
import { act } from "react-test-renderer";

describe("Test group hooks", () => {
  const jedis: Group = {
    name: "jedis",
    description: "May the force be with you",
    external: false,
    members: [],
    type: "xml",
    _links: {
      delete: {
        href: "/groups/jedis",
      },
      update: {
        href: "/groups/jedis",
      },
    },
    _embedded: {
      members: [],
    },
  };

  const jedisCollection = {
    _embedded: {
      groups: [jedis],
    },
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useGroups tests", () => {
    it("should return groups", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups", jedisCollection);
      const { result, waitFor } = renderHook(() => useGroups(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(jedisCollection);
    });

    it("should return paged groups", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups", jedisCollection, {
        query: {
          page: "42",
        },
      });
      const { result, waitFor } = renderHook(() => useGroups({ page: 42 }), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(jedisCollection);
    });

    it("should return searched groups", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups", jedisCollection, {
        query: {
          q: "jedis",
        },
      });
      const { result, waitFor } = renderHook(() => useGroups({ search: "jedis" }), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(jedisCollection);
    });

    it("should update group cache", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups", jedisCollection);
      const { result, waitFor } = renderHook(() => useGroups(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(queryClient.getQueryData(["group", "jedis"])).toEqual(jedis);
    });
  });

  describe("useGroup tests", () => {
    it("should return group", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups/jedis", jedis);
      const { result, waitFor } = renderHook(() => useGroup("jedis"), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(jedis);
    });
  });

  describe("useCreateGroup tests", () => {
    it("should create group", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");

      fetchMock.postOnce("/api/v2/groups", {
        status: 201,
        headers: {
          Location: "/groups/jedis",
        },
      });

      fetchMock.getOnce("/api/v2/groups/jedis", jedis);

      const { result, waitForNextUpdate } = renderHook(() => useCreateGroup(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { create } = result.current;
        create(jedis);
        return waitForNextUpdate();
      });

      expect(result.current.group).toEqual(jedis);
    });

    it("should fail without location header", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");

      fetchMock.postOnce("/api/v2/groups", {
        status: 201,
      });

      fetchMock.getOnce("/api/v2/groups/jedis", jedis);

      const { result, waitForNextUpdate } = renderHook(() => useCreateGroup(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { create } = result.current;
        create(jedis);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeDefined();
    });
  });

  describe("useDeleteGroup tests", () => {
    it("should delete group", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");

      fetchMock.deleteOnce("/api/v2/groups/jedis", {
        status: 200,
      });

      const { result, waitForNextUpdate } = renderHook(() => useDeleteGroup(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { remove } = result.current;
        remove(jedis);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isDeleted).toBe(true);
      expect(result.current.isLoading).toBe(false);
      expect(queryClient.getQueryData(["group", "jedis"])).toBeUndefined();
    });
  });

  describe("useUpdateGroup tests", () => {
    it("should update group", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");

      const newJedis = {
        ...jedis,
        description: "may the 4th be with you",
      };

      fetchMock.putOnce("/api/v2/groups/jedis", {
        status: 200,
      });

      fetchMock.getOnce("/api/v2/groups/jedis", newJedis);

      const { result, waitForNextUpdate } = renderHook(() => useUpdateGroup(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { update } = result.current;
        update(newJedis);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isUpdated).toBe(true);
      expect(result.current.isLoading).toBe(false);
      expect(queryClient.getQueryData(["group", "jedis"])).toBeUndefined();
      expect(queryClient.getQueryData(["groups"])).toBeUndefined();
    });
  });
});

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
import fetchMock from "fetch-mock";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import { renderHook, act, waitFor } from "@testing-library/react";
import { useCreateGroup, useDeleteGroup, useGroup, useGroups, useUpdateGroup } from "./groups";
import createWrapper from "./tests/createWrapper";

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
      const { result } = renderHook(() => useGroups(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(jedisCollection));
    });

    it("should return paged groups", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups", jedisCollection, {
        query: {
          page: "42",
        },
      });
      const { result } = renderHook(() => useGroups({ page: 42 }), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(jedisCollection));
    });

    it("should return searched groups", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups", jedisCollection, {
        query: {
          q: "jedis",
        },
      });
      const { result } = renderHook(() => useGroups({ search: "jedis" }), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(jedisCollection));
    });

    it("should update group cache", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups", jedisCollection);
      renderHook(() => useGroups(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(queryClient.getQueryData(["group", "jedis"])).toEqual(jedis));
    });
  });

  describe("useGroup tests", () => {
    it("should return group", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");
      fetchMock.get("/api/v2/groups/jedis", jedis);
      const { result } = renderHook(() => useGroup("jedis"), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(jedis));
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

      const { result } = renderHook(() => useCreateGroup(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.create(jedis);
      });

      await waitFor(() => {
        expect(result.current.group).toEqual(jedis);
      });
    });

    it("should fail without location header", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "groups", "/groups");

      fetchMock.postOnce("/api/v2/groups", {
        status: 201,
      });

      fetchMock.getOnce("/api/v2/groups/jedis", jedis);

      const { result } = renderHook(() => useCreateGroup(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.create(jedis);
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

      const { result } = renderHook(() => useDeleteGroup(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.remove(jedis);
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isDeleted).toBe(true);
      expect(result.current.isPending).toBe(false);
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

      const { result } = renderHook(() => useUpdateGroup(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.update(jedis);
      });

      expect(result.current.error).toBeFalsy();
      await waitFor(() => {
        expect(result.current.isUpdated).toBe(true);
        expect(result.current.isPending).toBe(false);
      });
      expect(queryClient.getQueryData(["group", "jedis"])).toBeUndefined();
      expect(queryClient.getQueryData(["groups"])).toBeUndefined();
    });
  });
});

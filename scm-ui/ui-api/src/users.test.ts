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

import { User, UserCollection } from "@scm-manager/ui-types";
import { renderHook, act, waitFor } from "@testing-library/react";
import fetchMock from "fetch-mock";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import createWrapper from "./tests/createWrapper";
import {
  useConvertToExternal,
  useConvertToInternal,
  useCreateUser,
  useDeleteUser,
  useUpdateUser,
  useUser,
  useUserLink,
  useUsers,
} from "./users";

describe("Test user hooks", () => {
  const yoda: User = {
    active: false,
    displayName: "",
    external: false,
    password: "",
    name: "yoda",
    _links: {
      delete: {
        href: "/users/yoda",
      },
      update: {
        href: "/users/yoda",
      },
      publicKeys: {
        href: "/users/yoda/publicKeys",
      },
      convertToInternal: {
        href: "/users/yoda/convertToInternal",
      },
      convertToExternal: {
        href: "/users/yoda/convertToExternal",
      },
    },
    _embedded: {
      members: [],
    },
  };

  const userCollection: UserCollection = {
    _links: {},
    page: 0,
    pageTotal: 0,
    _embedded: {
      users: [yoda],
    },
    externalAuthenticationAvailable: false,
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useUsers tests", () => {
    it("should return users", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users", userCollection);
      const { result } = renderHook(() => useUsers(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(userCollection));
    });

    it("should return paged users", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users", userCollection, {
        query: {
          page: "42",
        },
      });
      const { result } = renderHook(() => useUsers({ page: 42 }), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(userCollection));
    });

    it("should return searched users", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users", userCollection, {
        query: {
          q: "yoda",
        },
      });
      const { result } = renderHook(() => useUsers({ search: "yoda" }), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(userCollection));
    });

    it("should update user cache", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users", userCollection);
      renderHook(() => useUsers(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(queryClient.getQueryData(["user", "yoda"])).toEqual(yoda));
    });
  });

  describe("useUser tests", () => {
    it("should return user", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users/yoda", yoda);
      const { result } = renderHook(() => useUser("yoda"), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(yoda));
    });
  });

  describe("useUserLink tests", () => {
    it("should return specific user link", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users/yoda", yoda);
      const { result } = renderHook(() => useUserLink("yoda", "publicKeys"), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current).toBe("/users/yoda/publicKeys"));
    });

    it("should return undefined if link name does not exist", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users/yoda", yoda);
      const { result } = renderHook(() => useUserLink("yoda", "unknownLink"), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current).toBeUndefined());
    });

    it("should return undefined if user does not exist", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users/jinn", 404);
      const { result } = renderHook(() => useUserLink("jinn", "publicKeys"), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current).toBeUndefined());
    });
  });

  describe("useCreateUser tests", () => {
    it("should create user", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");

      fetchMock.postOnce("/api/v2/users", {
        status: 201,
        headers: {
          Location: "/users/yoda",
        },
      });

      fetchMock.getOnce("/api/v2/users/yoda", yoda);

      const { result } = renderHook(() => useCreateUser(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        result.current.create(yoda);
      });

      await waitFor(() => expect(result.current.user).toEqual(yoda));
    });

    it("should fail without location header", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");

      fetchMock.postOnce("/api/v2/users", {
        status: 201,
      });

      fetchMock.getOnce("/api/v2/users/yoda", yoda);

      const { result } = renderHook(() => useCreateUser(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        result.current.create(yoda);
      });

      expect(result.current.error).toBeDefined();
    });
  });

  describe("useDeleteUser tests", () => {
    it("should delete user", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");

      fetchMock.deleteOnce("/api/v2/users/yoda", {
        status: 200,
        body: {},
      });

      const { result } = renderHook(() => useDeleteUser(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        result.current.remove(yoda);
      });

      await waitFor(() => {
        expect(result.current.isDeleted).toBe(true);
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isPending).toBe(false);
      expect(queryClient.getQueryData(["user", "yoda"])).toBeUndefined();
    });
  });

  describe("useUpdateUser tests", () => {
    it("should update user", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");

      const newJedis = {
        ...yoda,
        description: "may the 4th be with you",
      };

      fetchMock.putOnce("/api/v2/users/yoda", {
        status: 200,
      });

      fetchMock.getOnce("/api/v2/users/yoda", newJedis);

      const { result } = renderHook(() => useUpdateUser(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        result.current.update(newJedis);
      });

      await waitFor(() => {
        expect(result.current.isUpdated).toBe(true);
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isPending).toBe(false);
      expect(queryClient.getQueryData(["user", "yoda"])).toBeUndefined();
      expect(queryClient.getQueryData(["users"])).toBeUndefined();
    });
  });

  describe("useConvertToInternal tests", () => {
    it("should convert user", async () => {
      const queryClient = createInfiniteCachingClient();

      fetchMock.putOnce("/api/v2/users/yoda/convertToInternal", {
        status: 200,
      });

      const { result } = renderHook(() => useConvertToInternal(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        const { convertToInternal } = result.current;
        convertToInternal(yoda, "thisisaverystrongpassword");
      });

      expect(result.current.error).toBeFalsy();
      await waitFor(() => {
        expect(result.current.isConverted).toBe(true);
        expect(result.current.isPending).toBe(false);
      });

      expect(queryClient.getQueryData(["user", "yoda"])).toBeUndefined();
      expect(queryClient.getQueryData(["users"])).toBeUndefined();
    });
  });

  describe("useConvertToExternal tests", () => {
    it("should convert user", async () => {
      const queryClient = createInfiniteCachingClient();

      fetchMock.putOnce("/api/v2/users/yoda/convertToExternal", {
        status: 200,
      });

      const { result } = renderHook(() => useConvertToExternal(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        const { convertToExternal } = result.current;
        convertToExternal(yoda);
      });

      expect(result.current.error).toBeFalsy();
      await waitFor(() => expect(result.current.isConverted).toBe(true));
      expect(result.current.isPending).toBe(false);
      expect(queryClient.getQueryData(["user", "yoda"])).toBeUndefined();
      expect(queryClient.getQueryData(["users"])).toBeUndefined();
    });
  });
});

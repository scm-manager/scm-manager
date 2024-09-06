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
import fetchMock from "fetch-mock-jest";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import { act } from "react-test-renderer";
import {
  useConvertToExternal,
  useConvertToInternal,
  useCreateUser,
  useDeleteUser,
  useUpdateUser,
  useUser,
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
      const { result, waitFor } = renderHook(() => useUsers(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(userCollection);
    });

    it("should return paged users", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users", userCollection, {
        query: {
          page: "42",
        },
      });
      const { result, waitFor } = renderHook(() => useUsers({ page: 42 }), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(userCollection);
    });

    it("should return searched users", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users", userCollection, {
        query: {
          q: "yoda",
        },
      });
      const { result, waitFor } = renderHook(() => useUsers({ search: "yoda" }), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(userCollection);
    });

    it("should update user cache", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users", userCollection);
      const { result, waitFor } = renderHook(() => useUsers(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(queryClient.getQueryData(["user", "yoda"])).toEqual(yoda);
    });
  });

  describe("useUser tests", () => {
    it("should return user", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");
      fetchMock.get("/api/v2/users/yoda", yoda);
      const { result, waitFor } = renderHook(() => useUser("yoda"), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(yoda);
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

      const { result, waitForNextUpdate } = renderHook(() => useCreateUser(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { create } = result.current;
        create(yoda);
        return waitForNextUpdate();
      });

      expect(result.current.user).toEqual(yoda);
    });

    it("should fail without location header", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "users", "/users");

      fetchMock.postOnce("/api/v2/users", {
        status: 201,
      });

      fetchMock.getOnce("/api/v2/users/yoda", yoda);

      const { result, waitForNextUpdate } = renderHook(() => useCreateUser(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { create } = result.current;
        create(yoda);
        return waitForNextUpdate();
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
      });

      const { result, waitForNextUpdate } = renderHook(() => useDeleteUser(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { remove } = result.current;
        remove(yoda);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isDeleted).toBe(true);
      expect(result.current.isLoading).toBe(false);
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

      const { result, waitForNextUpdate } = renderHook(() => useUpdateUser(), {
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

      const { result, waitForNextUpdate } = renderHook(() => useConvertToInternal(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { convertToInternal } = result.current;
        convertToInternal(yoda, "thisisaverystrongpassword");
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isConverted).toBe(true);
      expect(result.current.isLoading).toBe(false);
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

      const { result, waitForNextUpdate } = renderHook(() => useConvertToExternal(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { convertToExternal } = result.current;
        convertToExternal(yoda);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isConverted).toBe(true);
      expect(result.current.isLoading).toBe(false);
      expect(queryClient.getQueryData(["user", "yoda"])).toBeUndefined();
      expect(queryClient.getQueryData(["users"])).toBeUndefined();
    });
  });
});

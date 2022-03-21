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

import fetchMock from "fetch-mock-jest";
import { renderHook } from "@testing-library/react-hooks";
import { Me } from "@scm-manager/ui-types";
import createWrapper from "./tests/createWrapper";
import { useLogin, useLogout, useMe, useRequiredMe, useSubject } from "./login";
import { setEmptyIndex, setIndexLink } from "./tests/indexLinks";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { LegacyContext } from "./LegacyContext";
import { act } from "react-test-renderer";

describe("Test login hooks", () => {
  const tricia: Me = {
    name: "tricia",
    displayName: "Tricia",
    groups: [],
    _links: {}
  };

  describe("useMe tests", () => {
    fetchMock.get("/api/v2/me", {
      name: "tricia",
      displayName: "Tricia",
      groups: [],
      _links: {}
    });

    it("should return me", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "me", "/me");

      const { result, waitFor } = renderHook(() => useMe(), { wrapper: createWrapper(undefined, queryClient) });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current?.data?.name).toEqual("tricia");
    });

    it("should call onMeFetched of LegacyContext", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "me", "/me");

      let me: Me;
      const context: LegacyContext = {
        onMeFetched: fetchedMe => {
          me = fetchedMe;
        },
        initialize: () => null
      };

      const { result, waitFor } = renderHook(() => useMe(), { wrapper: createWrapper(context, queryClient) });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(me!.name).toEqual("tricia");
    });

    it("should return nothing without me link", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);

      const { result } = renderHook(() => useMe(), { wrapper: createWrapper(undefined, queryClient) });

      expect(result.current.isLoading).toBe(false);
      expect(result.current?.data).toBeFalsy();
      expect(result.current?.error).toBeFalsy();
    });
  });

  describe("useRequiredMe tests", () => {
    it("should return me", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData("me", tricia);
      setIndexLink(queryClient, "me", "/me");
      const { result, waitFor } = renderHook(() => useRequiredMe(), { wrapper: createWrapper(undefined, queryClient) });
      await waitFor(() => {
        return !!result.current;
      });
      expect(result.current?.name).toBe("tricia");
    });

    it("should throw an error if me is not available", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);

      const { result } = renderHook(() => useRequiredMe(), { wrapper: createWrapper(undefined, queryClient) });

      expect(result.error).toBeDefined();
    });
  });

  describe("useSubject tests", () => {
    it("should return authenticated subject", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);
      queryClient.setQueryData("me", tricia);
      const { result } = renderHook(() => useSubject(), { wrapper: createWrapper(undefined, queryClient) });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.isAnonymous).toBe(false);
      expect(result.current.me).toEqual(tricia);
    });

    it("should return anonymous subject", () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "login", "/login");
      queryClient.setQueryData("me", {
        name: "_anonymous",
        displayName: "Anonymous",
        groups: [],
        _links: {}
      });
      const { result } = renderHook(() => useSubject(), { wrapper: createWrapper(undefined, queryClient) });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isAnonymous).toBe(true);
    });

    it("should return unauthenticated subject", () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "login", "/login");
      const { result } = renderHook(() => useSubject(), { wrapper: createWrapper(undefined, queryClient) });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isAnonymous).toBe(false);
    });
  });

  describe("useLogin tests", () => {
    it("should login", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "login", "/login");

      fetchMock.post("/api/v2/login", "", {
        body: {
          cookie: true,
          grant_type: "password",
          username: "tricia",
          password: "hitchhikersSecret!"
        }
      });

      // required because we invalidate the whole cache and react-query refetches the index
      fetchMock.get("/api/v2/", {
        version: "x.y.z",
        _links: {
          login: {
            href: "/second/login"
          }
        }
      });

      const { result, waitForNextUpdate } = renderHook(() => useLogin(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      const { login } = result.current;
      expect(login).toBeDefined();

      await act(() => {
        if (login) {
          login("tricia", "hitchhikersSecret!");
        }
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
    });

    it("should not return login, if authenticated", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);
      queryClient.setQueryData("me", tricia);

      const { result } = renderHook(() => useLogin(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      expect(result.current.login).toBeUndefined();
    });
  });

  describe("useLogout tests", () => {
    it("should call logout", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "logout", "/logout");

      fetchMock.deleteOnce("/api/v2/logout", {});

      const { result, waitForNextUpdate } = renderHook(() => useLogout(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      const { logout } = result.current;
      expect(logout).toBeDefined();

      await act(() => {
        if (logout) {
          logout();
        }
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
    });

    it("should not return logout without link", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);

      const { result } = renderHook(() => useLogout(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      const { logout } = result.current;
      expect(logout).toBeUndefined();
    });
  });
});

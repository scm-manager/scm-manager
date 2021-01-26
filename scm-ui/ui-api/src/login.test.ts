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
import { useLogin, useMe, useSubject } from "./login";
import { setIndexLink, setEmptyIndex } from "./tests/indexLinks";
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
        }
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

      expect(result.current.isAuthenticated).toBe(true);
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
          password: "hitchhickersSecret!"
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

      await act(() => {
        login("tricia", "hitchhickersSecret!");
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
    });
  });
});

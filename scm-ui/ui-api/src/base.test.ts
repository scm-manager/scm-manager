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
import { useIndex, useIndexJsonResource, useIndexLink, useIndexLinks, useRequiredIndexLink, useVersion } from "./base";
import { renderHook } from "@testing-library/react-hooks";
import { LegacyContext } from "./LegacyContext";
import { IndexResources, Link } from "@scm-manager/ui-types";
import createWrapper from "./tests/createWrapper";
import { QueryClient } from "react-query";

describe("Test base api hooks", () => {
  describe("useIndex tests", () => {
    fetchMock.get("/api/v2/", {
      version: "x.y.z",
      _links: {}
    });

    it("should return index", async () => {
      const { result, waitFor } = renderHook(() => useIndex(), { wrapper: createWrapper() });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current?.data?.version).toEqual("x.y.z");
    });

    it("should call onIndexFetched of LegacyContext", async () => {
      let index: IndexResources;
      const context: LegacyContext = {
        onIndexFetched: fetchedIndex => {
          index = fetchedIndex;
        },
        initialize: () => null
      };
      const { result, waitFor } = renderHook(() => useIndex(), { wrapper: createWrapper(context) });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(index!.version).toEqual("x.y.z");
    });
  });

  describe("useIndexLink tests", () => {
    it("should throw an error if index is not available", () => {
      const { result } = renderHook(() => useIndexLink("spaceships"), { wrapper: createWrapper() });
      expect(result.error).toBeDefined();
    });

    it("should return undefined for unknown link", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData("index", {
        version: "x.y.z",
        _links: {}
      });
      const { result } = renderHook(() => useIndexLink("spaceships"), {
        wrapper: createWrapper(undefined, queryClient)
      });
      expect(result.current).toBeUndefined();
    });

    it("should return undefined for link array", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData("index", {
        version: "x.y.z",
        _links: {
          spaceships: [
            {
              name: "heartOfGold",
              href: "/spaceships/heartOfGold"
            },
            {
              name: "razorCrest",
              href: "/spaceships/razorCrest"
            }
          ]
        }
      });
      const { result } = renderHook(() => useIndexLink("spaceships"), {
        wrapper: createWrapper(undefined, queryClient)
      });
      expect(result.current).toBeUndefined();
    });

    it("should return link", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData("index", {
        version: "x.y.z",
        _links: {
          spaceships: {
            href: "/api/spaceships"
          }
        }
      });
      const { result } = renderHook(() => useIndexLink("spaceships"), {
        wrapper: createWrapper(undefined, queryClient)
      });
      expect(result.current).toBe("/api/spaceships");
    });
  });

  describe("useIndexLinks tests", () => {
    it("should throw an error if index is not available", async () => {
      const { result } = renderHook(() => useIndexLinks(), { wrapper: createWrapper() });
      expect(result.error).toBeDefined();
    });

    it("should return links", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData("index", {
        version: "x.y.z",
        _links: {
          spaceships: {
            href: "/api/spaceships"
          }
        }
      });
      const { result } = renderHook(() => useIndexLinks(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      expect((result.current!.spaceships as Link).href).toBe("/api/spaceships");
    });
  });

  describe("useVersion tests", () => {
    it("should throw an error if version is not available", async () => {
      const { result } = renderHook(() => useVersion(), { wrapper: createWrapper() });
      expect(result.error).toBeDefined();
    });

    it("should return version", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData("index", {
        version: "x.y.z"
      });
      const { result } = renderHook(() => useVersion(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      expect(result.current).toBe("x.y.z");
    });
  });

  describe("useRequiredIndexLink tests", () => {
    it("should throw error for undefined link", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData("index", {
        version: "x.y.z",
        _links: {}
      });
      const { result } = renderHook(() => useRequiredIndexLink("spaceships"), {
        wrapper: createWrapper(undefined, queryClient)
      });
      expect(result.error).toBeDefined();
    });

    it("should return link", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData("index", {
        version: "x.y.z",
        _links: {
          spaceships: {
            href: "/api/spaceships"
          }
        }
      });
      const { result } = renderHook(() => useRequiredIndexLink("spaceships"), {
        wrapper: createWrapper(undefined, queryClient)
      });
      expect(result.current).toBe("/api/spaceships");
    });
  });

  describe("useIndexJsonResource tests", () => {
    it("should return json resource from link", async () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData("index", {
        version: "x.y.z",
        _links: {
          spaceships: {
            href: "/spaceships"
          }
        }
      });

      const spaceship = {
        name: "heartOfGold"
      };

      fetchMock.get("/api/v2/spaceships", spaceship);

      const { result, waitFor } = renderHook(() => useIndexJsonResource<typeof spaceship>("spaceships"), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      expect(result.current.data!.name).toBe("heartOfGold");
    });
  });

  it("should return nothing if link is not available", () => {
    const queryClient = new QueryClient();
    queryClient.setQueryData("index", {
      version: "x.y.z",
      _links: {}
    });

    const { result } = renderHook(() => useIndexJsonResource<Record<string, unknown>>("spaceships"), {
      wrapper: createWrapper(undefined, queryClient)
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.error).toBeFalsy();
    expect(result.current.data).toBeFalsy();
  });
});

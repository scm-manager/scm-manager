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
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import fetchMock from "fetch-mock-jest";
import { renderHook } from "@testing-library/react-hooks";
import { useNamespace, useNamespaces, useNamespaceStrategies } from "./namespaces";
import createWrapper from "./tests/createWrapper";

describe("Test namespace hooks", () => {
  describe("useNamespaces test", () => {
    it("should return namespaces", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "namespaces", "/namespaces");
      fetchMock.get("/api/v2/namespaces", {
        _embedded: {
          namespaces: [
            {
              namespace: "spaceships",
              _links: {},
            },
          ],
        },
      });

      const { result, waitFor } = renderHook(() => useNamespaces(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current?.data?._embedded.namespaces[0].namespace).toBe("spaceships");
    });
  });

  describe("useNamespaceStrategies tests", () => {
    it("should return namespaces strategies", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "namespaceStrategies", "/ns");
      fetchMock.get("/api/v2/ns", {
        current: "awesome",
        available: [],
        _links: {},
      });

      const { result, waitFor } = renderHook(() => useNamespaceStrategies(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current?.data?.current).toEqual("awesome");
    });
  });

  describe("useNamespace tests", () => {
    it("should return namespace", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "namespaces", "/ns");
      fetchMock.get("/api/v2/ns/awesome", {
        namespace: "awesome",
        _links: {},
      });

      const { result, waitFor } = renderHook(() => useNamespace("awesome"), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current?.data?.namespace).toEqual("awesome");
    });
  });
});

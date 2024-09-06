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

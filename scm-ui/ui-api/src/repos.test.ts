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
import createWrapper from "./tests/createWrapper";
import { setIndexLink } from "./tests/indexLinks";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import {
  useCreateRepository,
  useNamespaces,
  useNamespaceStrategies,
  useRepositories,
  UseRepositoriesRequest,
  useRepository,
  useRepositoryTypes
} from "./repos";
import { Repository } from "@scm-manager/ui-types";
import { QueryClient } from "react-query";
import { act } from "react-test-renderer";

describe("Test repository hooks", () => {
  const heartOfGold: Repository = {
    namespace: "spaceships",
    name: "heartOfGold",
    type: "git",
    _links: {}
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useRepositories tests", () => {
    const repositoryCollection = {
      _embedded: {
        repositories: [heartOfGold],
        _links: {}
      }
    };

    const expectCollection = async (queryClient: QueryClient, request?: UseRepositoriesRequest) => {
      const { result, waitFor } = renderHook(() => useRepositories(request), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current.data).toEqual(repositoryCollection);
    };

    it("should return repositories", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/repos", repositoryCollection, {
        query: {
          sortBy: "namespaceAndName"
        }
      });

      await expectCollection(queryClient);
    });

    it("should return repositories with page", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/repos", repositoryCollection, {
        query: {
          sortBy: "namespaceAndName",
          page: "42"
        }
      });

      await expectCollection(queryClient, {
        page: 42
      });
    });

    it("should use repository from namespace", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/spaceships", repositoryCollection, {
        query: {
          sortBy: "namespaceAndName"
        }
      });

      await expectCollection(queryClient, {
        namespace: {
          namespace: "spaceships",
          _links: {
            repositories: {
              href: "/spaceships"
            }
          }
        }
      });
    });

    it("should append search query", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/repos", repositoryCollection, {
        query: {
          sortBy: "namespaceAndName",
          q: "heart"
        }
      });

      await expectCollection(queryClient, {
        search: "heart"
      });
    });

    it("should update repository cache", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/repos", repositoryCollection, {
        query: {
          sortBy: "namespaceAndName"
        }
      });

      await expectCollection(queryClient);

      const repository = queryClient.getQueryData(["repository", "spaceships", "heartOfGold"]);
      expect(repository).toEqual(heartOfGold);
    });

    it("should return nothing if disabled", () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      const { result } = renderHook(() => useRepositories({ disabled: true }), {
        wrapper: createWrapper(undefined, queryClient)
      });

      expect(result.current.isLoading).toBe(false);
      expect(result.current.data).toBeFalsy();
      expect(result.current.error).toBeFalsy();
    });
  });

  describe("useCreateRepository tests", () => {
    it("should create repository", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/r");

      fetchMock.postOnce("/api/v2/r", {
        status: 201,
        headers: {
          Location: "/r/spaceships/heartOfGold"
        }
      });

      fetchMock.getOnce("/api/v2/r/spaceships/heartOfGold", heartOfGold);

      const { result, waitForNextUpdate } = renderHook(() => useCreateRepository(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      const repository = {
        ...heartOfGold,
        contextEntries: []
      };

      await act(() => {
        const { create } = result.current;
        create(repository, false);
        return waitForNextUpdate();
      });

      expect(result.current.repository).toEqual(heartOfGold);
    });

    it("should append initialize param", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/r");

      fetchMock.postOnce("/api/v2/r?initialize=true", {
        status: 201,
        headers: {
          Location: "/r/spaceships/heartOfGold"
        }
      });

      fetchMock.getOnce("/api/v2/r/spaceships/heartOfGold", heartOfGold);

      const { result, waitForNextUpdate } = renderHook(() => useCreateRepository(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      const repository = {
        ...heartOfGold,
        contextEntries: []
      };

      await act(() => {
        const { create } = result.current;
        create(repository, true);
        return waitForNextUpdate();
      });

      expect(result.current.repository).toEqual(heartOfGold);
    });

    it("should fail without location header", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/r");

      fetchMock.postOnce("/api/v2/r", {
        status: 201
      });

      const { result, waitForNextUpdate } = renderHook(() => useCreateRepository(), {
        wrapper: createWrapper(undefined, queryClient)
      });

      const repository = {
        ...heartOfGold,
        contextEntries: []
      };

      await act(() => {
        const { create } = result.current;
        create(repository, false);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeDefined();
    });
  });

  describe("useNamespaces test", () => {
    it("should return namespaces", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "namespaces", "/namespaces");
      fetchMock.get("/api/v2/namespaces", {
        _embedded: {
          namespaces: [
            {
              namespace: "spaceships",
              _links: {}
            }
          ]
        }
      });

      const { result, waitFor } = renderHook(() => useNamespaces(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current?.data?._embedded.namespaces[0].namespace).toBe("spaceships");
    });
  });

  describe("useRepository tests", () => {
    it("should return repository", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/r");
      fetchMock.get("/api/v2/r/spaceships/heartOfGold", heartOfGold);

      const { result, waitFor } = renderHook(() => useRepository("spaceships", "heartOfGold"), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current?.data?.type).toEqual("git");
    });
  });

  describe("useRepositoryTypes tests", () => {
    it("should return repository types", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositoryTypes", "/rt");
      fetchMock.get("/api/v2/rt", {
        _embedded: {
          repositoryTypes: [
            {
              name: "git",
              displayName: "Git",
              _links: {}
            }
          ]
        },
        _links: {}
      });

      const { result, waitFor } = renderHook(() => useRepositoryTypes(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.repositoryTypes;
      });
      expect(result.current?.repositoryTypes[0].name).toEqual("git");
    });
  });

  describe("useNamespaceStrategies tests", () => {
    it("should return namespaces strategies", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "namespaceStrategies", "/ns");
      fetchMock.get("/api/v2/ns", {
        current: "awesome",
        available: [],
        _links: {}
      });

      const { result, waitFor } = renderHook(() => useNamespaceStrategies(), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current?.data?.current).toEqual("awesome");
    });
  });
});

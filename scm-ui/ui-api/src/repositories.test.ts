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
  useArchiveRepository,
  useCreateRepository,
  useDeleteRepository,
  UseDeleteRepositoryOptions,
  useRepositories,
  UseRepositoriesRequest,
  useRepository,
  useRepositoryTypes,
  useUnarchiveRepository,
  useUpdateRepository,
} from "./repositories";
import { Repository } from "@scm-manager/ui-types";
import { QueryClient } from "react-query";
import { act } from "react-test-renderer";

describe("Test repository hooks", () => {
  const heartOfGold: Repository = {
    namespace: "spaceships",
    name: "heartOfGold",
    type: "git",
    _links: {
      delete: {
        href: "/r/spaceships/heartOfGold",
      },
      update: {
        href: "/r/spaceships/heartOfGold",
      },
      archive: {
        href: "/r/spaceships/heartOfGold/archive",
      },
      unarchive: {
        href: "/r/spaceships/heartOfGold/unarchive",
      },
    },
  };

  const repositoryCollection = {
    _embedded: {
      repositories: [heartOfGold],
    },
    _links: {},
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useRepositories tests", () => {
    const expectCollection = async (queryClient: QueryClient, request?: UseRepositoriesRequest) => {
      const { result, waitFor } = renderHook(() => useRepositories(request), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current.data).toEqual(repositoryCollection);
    };

    it("should return repositories", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/repos", repositoryCollection);

      await expectCollection(queryClient);
    });

    it("should return repositories with page", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/repos", repositoryCollection, {
        query: {
          page: "42",
        },
      });

      await expectCollection(queryClient, {
        page: 42,
      });
    });

    it("should use repository from namespace", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/spaceships", repositoryCollection);

      await expectCollection(queryClient, {
        namespace: {
          namespace: "spaceships",
          _links: {
            repositories: {
              href: "/spaceships",
            },
          },
        },
      });
    });

    it("should append search query", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/repos", repositoryCollection, {
        query: {
          q: "heart",
        },
      });

      await expectCollection(queryClient, {
        search: "heart",
      });
    });

    it("should update repository cache", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      fetchMock.get("/api/v2/repos", repositoryCollection);

      await expectCollection(queryClient);

      const repository = queryClient.getQueryData(["repository", "spaceships", "heartOfGold"]);
      expect(repository).toEqual(heartOfGold);
    });

    it("should return nothing if disabled", () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/repos");
      const { result } = renderHook(() => useRepositories({ disabled: true }), {
        wrapper: createWrapper(undefined, queryClient),
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
          Location: "/r/spaceships/heartOfGold",
        },
      });

      fetchMock.getOnce("/api/v2/r/spaceships/heartOfGold", heartOfGold);

      const { result, waitForNextUpdate } = renderHook(() => useCreateRepository(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      const repository = {
        ...heartOfGold,
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
          Location: "/r/spaceships/heartOfGold",
        },
      });

      fetchMock.getOnce("/api/v2/r/spaceships/heartOfGold", heartOfGold);

      const { result, waitForNextUpdate } = renderHook(() => useCreateRepository(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      const repository = {
        ...heartOfGold,
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
        status: 201,
      });

      const { result, waitForNextUpdate } = renderHook(() => useCreateRepository(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      const repository = {
        ...heartOfGold,
      };

      await act(() => {
        const { create } = result.current;
        create(repository, false);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeDefined();
    });
  });

  describe("useRepository tests", () => {
    it("should return repository", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "repositories", "/r");
      fetchMock.get("/api/v2/r/spaceships/heartOfGold", heartOfGold);

      const { result, waitFor } = renderHook(() => useRepository("spaceships", "heartOfGold"), {
        wrapper: createWrapper(undefined, queryClient),
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
              _links: {},
            },
          ],
        },
        _links: {},
      });

      const { result, waitFor } = renderHook(() => useRepositoryTypes(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => {
        return !!result.current.data;
      });
      expect(result.current.data).toBeDefined();
      if (result.current?.data) {
        expect(result.current?.data._embedded?.repositoryTypes[0].name).toEqual("git");
      }
    });
  });

  describe("useDeleteRepository tests", () => {
    const queryClient = createInfiniteCachingClient();

    beforeEach(() => {
      queryClient.clear();
    });

    const deleteRepository = async (options?: UseDeleteRepositoryOptions) => {
      fetchMock.deleteOnce("/api/v2/r/spaceships/heartOfGold", {
        status: 204,
      });

      const { result, waitForNextUpdate } = renderHook(() => useDeleteRepository(options), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { remove } = result.current;
        remove(heartOfGold);
        return waitForNextUpdate();
      });

      return result.current;
    };

    const shouldRemoveQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await deleteRepository();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState).toBeUndefined();
    };

    const shouldInvalidateQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await deleteRepository();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState!.isInvalidated).toBe(true);
    };

    it("should delete repository", async () => {
      const { isDeleted } = await deleteRepository();

      expect(isDeleted).toBe(true);
    });

    it("should invalidate repository cache", async () => {
      await shouldRemoveQuery(["repository", "spaceships", "heartOfGold"], heartOfGold);
    });

    it("should invalidate repository collection cache", async () => {
      await shouldInvalidateQuery(["repositories"], repositoryCollection);
    });

    it("should call onSuccess callback", async () => {
      let repo;
      await deleteRepository({
        onSuccess: (repository) => {
          repo = repository;
        },
      });
      expect(repo).toEqual(heartOfGold);
    });
  });

  describe("useUpdateRepository tests", () => {
    const queryClient = createInfiniteCachingClient();

    beforeEach(() => {
      queryClient.clear();
    });

    const updateRepository = async () => {
      fetchMock.putOnce("/api/v2/r/spaceships/heartOfGold", {
        status: 204,
      });

      const { result, waitForNextUpdate } = renderHook(() => useUpdateRepository(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { update } = result.current;
        update(heartOfGold);
        return waitForNextUpdate();
      });

      return result.current;
    };

    const shouldInvalidateQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await updateRepository();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState!.isInvalidated).toBe(true);
    };

    it("should update repository", async () => {
      const { isUpdated } = await updateRepository();

      expect(isUpdated).toBe(true);
    });

    it("should invalidate repository cache", async () => {
      await shouldInvalidateQuery(["repository", "spaceships", "heartOfGold"], heartOfGold);
    });

    it("should invalidate repository collection cache", async () => {
      await shouldInvalidateQuery(["repositories"], repositoryCollection);
    });
  });

  describe("useArchiveRepository tests", () => {
    const queryClient = createInfiniteCachingClient();

    beforeEach(() => {
      queryClient.clear();
    });

    const archiveRepository = async () => {
      fetchMock.postOnce("/api/v2/r/spaceships/heartOfGold/archive", {
        status: 204,
      });

      const { result, waitForNextUpdate } = renderHook(() => useArchiveRepository(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { archive } = result.current;
        archive(heartOfGold);
        return waitForNextUpdate();
      });

      return result.current;
    };

    const shouldInvalidateQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await archiveRepository();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState!.isInvalidated).toBe(true);
    };

    it("should archive repository", async () => {
      const { isArchived } = await archiveRepository();

      expect(isArchived).toBe(true);
    });

    it("should invalidate repository cache", async () => {
      await shouldInvalidateQuery(["repository", "spaceships", "heartOfGold"], heartOfGold);
    });

    it("should invalidate repository collection cache", async () => {
      await shouldInvalidateQuery(["repositories"], repositoryCollection);
    });
  });

  describe("useUnarchiveRepository tests", () => {
    const queryClient = createInfiniteCachingClient();

    beforeEach(() => {
      queryClient.clear();
    });

    const unarchiveRepository = async () => {
      fetchMock.postOnce("/api/v2/r/spaceships/heartOfGold/unarchive", {
        status: 204,
      });

      const { result, waitForNextUpdate } = renderHook(() => useUnarchiveRepository(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { unarchive } = result.current;
        unarchive(heartOfGold);
        return waitForNextUpdate();
      });

      return result.current;
    };

    const shouldInvalidateQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await unarchiveRepository();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState!.isInvalidated).toBe(true);
    };

    it("should unarchive repository", async () => {
      const { isUnarchived } = await unarchiveRepository();

      expect(isUnarchived).toBe(true);
    });

    it("should invalidate repository cache", async () => {
      await shouldInvalidateQuery(["repository", "spaceships", "heartOfGold"], heartOfGold);
    });

    it("should invalidate repository collection cache", async () => {
      await shouldInvalidateQuery(["repositories"], repositoryCollection);
    });
  });
});

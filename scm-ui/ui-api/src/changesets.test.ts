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
import { Branch, Changeset, ChangesetCollection, Repository } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock-jest";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import { useChangeset, useChangesets } from "./changesets";

describe("Test changeset hooks", () => {
  const repository: Repository = {
    namespace: "hitchhiker",
    name: "heart-of-gold",
    type: "hg",
    _links: {
      changesets: {
        href: "/r/c",
      },
    },
  };

  const develop: Branch = {
    name: "develop",
    revision: "42",
    lastCommitter: { name: "trillian" },
    _links: {
      history: {
        href: "/r/b/c",
      },
    },
  };

  const changeset: Changeset = {
    id: "42",
    description: "Awesome change",
    date: new Date(),
    author: {
      name: "Arthur Dent",
    },
    _embedded: {},
    _links: {},
  };

  const changesets: ChangesetCollection = {
    page: 1,
    pageTotal: 1,
    _embedded: {
      changesets: [changeset],
    },
    _links: {},
  };

  const expectChangesetCollection = (result?: ChangesetCollection) => {
    expect(result?._embedded?.changesets[0].id).toBe(changesets._embedded?.changesets[0].id);
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useChangesets tests", () => {
    it("should return changesets", async () => {
      fetchMock.getOnce("/api/v2/r/c", changesets);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useChangesets(repository), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      expectChangesetCollection(result.current.data);
    });

    it("should return changesets for page", async () => {
      fetchMock.getOnce("/api/v2/r/c", changesets, {
        query: {
          page: 42,
        },
      });

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useChangesets(repository, { page: 42 }), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      expectChangesetCollection(result.current.data);
    });

    it("should use link from branch", async () => {
      fetchMock.getOnce("/api/v2/r/b/c", changesets);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useChangesets(repository, { branch: develop }), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      expectChangesetCollection(result.current.data);
    });

    it("should populate changeset cache", async () => {
      fetchMock.getOnce("/api/v2/r/c", changesets);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useChangesets(repository), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      const changeset: Changeset | undefined = queryClient.getQueryData([
        "repository",
        "hitchhiker",
        "heart-of-gold",
        "changeset",
        "42",
      ]);

      expect(changeset?.id).toBe("42");
    });
  });

  describe("useChangeset tests", () => {
    it("should return changes", async () => {
      fetchMock.get("/api/v2/r/c/42", changeset);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useChangeset(repository, "42"), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      const c = result.current.data;
      expect(c?.description).toBe("Awesome change");
    });
  });
});

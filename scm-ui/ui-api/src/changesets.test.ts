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
        href: "/r/c"
      }
    }
  };

  const develop: Branch = {
    name: "develop",
    revision: "42",
    lastCommitter: { name: "trillian" },
    _links: {
      history: {
        href: "/r/b/c"
      }
    }
  };

  const changeset: Changeset = {
    id: "42",
    description: "Awesome change",
    date: new Date(),
    author: {
      name: "Arthur Dent"
    },
    _embedded: {},
    _links: {}
  };

  const changesets: ChangesetCollection = {
    page: 1,
    pageTotal: 1,
    _embedded: {
      changesets: [changeset]
    },
    _links: {}
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
        wrapper: createWrapper(undefined, queryClient)
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      expectChangesetCollection(result.current.data);
    });

    it("should return changesets for page", async () => {
      fetchMock.getOnce("/api/v2/r/c", changesets, {
        query: {
          page: 42
        }
      });

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useChangesets(repository, { page: 42 }), {
        wrapper: createWrapper(undefined, queryClient)
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
        wrapper: createWrapper(undefined, queryClient)
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
        wrapper: createWrapper(undefined, queryClient)
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      const changeset: Changeset | undefined = queryClient.getQueryData([
        "repository",
        "hitchhiker",
        "heart-of-gold",
        "changeset",
        "42"
      ]);

      expect(changeset?.id).toBe("42");
    });
  });

  describe("useChangeset tests", () => {
    it("should return changes", async () => {
      fetchMock.get("/api/v2/r/c/42", changeset);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useChangeset(repository, "42"), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await waitFor(() => {
        return !!result.current.data;
      });

      const c = result.current.data;
      expect(c?.description).toBe("Awesome change");
    });
  });
});

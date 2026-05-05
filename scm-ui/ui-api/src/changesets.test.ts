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

import { Changeset, ChangesetCollection, Repository } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { renderHook, waitFor } from "@testing-library/react";
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

      const { result } = renderHook(() => useChangesets(repository), {
        wrapper: createWrapper(queryClient),
      });

      await waitFor(() => {
        return expectChangesetCollection(result.current.data);
      });
    });

    it("should return changesets for page", async () => {
      fetchMock.getOnce("/api/v2/r/c?page=42", changesets);

      const queryClient = createInfiniteCachingClient();

      const { result } = renderHook(() => useChangesets(repository, { page: 42 }), {
        wrapper: createWrapper(queryClient),
      });

      await waitFor(() => {
        return expectChangesetCollection(result.current.data);
      });
    });

    it("should use link from branch", async () => {
      fetchMock.getOnce("/api/v2/r/c?revision=develop", changesets);

      const queryClient = createInfiniteCachingClient();

      const { result } = renderHook(() => useChangesets(repository, { revision: "develop" }), {
        wrapper: createWrapper(queryClient),
      });

      await waitFor(() => {
        return expectChangesetCollection(result.current.data);
      });
    });

    it("should populate changeset cache", async () => {
      fetchMock.getOnce("/api/v2/r/c", changesets);

      const queryClient = createInfiniteCachingClient();

      renderHook(() => useChangesets(repository), {
        wrapper: createWrapper(queryClient),
      });

      await waitFor(() => {
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
  });

  describe("useChangeset tests", () => {
    it("should return changes", async () => {
      fetchMock.get("/api/v2/r/c/42", changeset);

      const queryClient = createInfiniteCachingClient();

      const { result } = renderHook(() => useChangeset(repository, "42"), {
        wrapper: createWrapper(queryClient),
      });

      await waitFor(() => {
        expect(result.current.data?.description).toBe("Awesome change");
      });
    });
  });
});

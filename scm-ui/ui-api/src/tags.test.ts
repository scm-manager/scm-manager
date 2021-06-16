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
import { Changeset, Repository, Tag, TagCollection } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock-jest";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { useCreateTag, useDeleteTag, useTag, useTags } from "./tags";
import { act } from "react-test-renderer";

describe("Test Tag hooks", () => {
  const repository: Repository = {
    namespace: "hitchhiker",
    name: "heart-of-gold",
    type: "git",
    _links: {
      tags: {
        href: "/hog/tags"
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
    _links: {
      tag: {
        href: "/hog/tag"
      }
    }
  };

  const tagOneDotZero = {
    name: "1.0",
    revision: "42",
    signatures: [],
    _links: {
      "delete": {
        href: "/hog/tags/1.0"
      }
    }
  };

  const tags: TagCollection = {
    _embedded: {
      tags: [tagOneDotZero]
    },
    _links: {}
  };

  const queryClient = createInfiniteCachingClient();

  beforeEach(() => queryClient.clear());

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useTags tests", () => {
    const fetchTags = async () => {
      fetchMock.getOnce("/api/v2/hog/tags", tags);

      const { result, waitFor } = renderHook(() => useTags(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });

      return result.current;
    };

    it("should return tags", async () => {
      const { data } = await fetchTags();
      expect(data).toEqual(tags);
    });

    it("should cache tag collection", async () => {
      await fetchTags();

      const cachedTags = queryClient.getQueryData(["repository", "hitchhiker", "heart-of-gold", "tags"]);
      expect(cachedTags).toEqual(tags);
    });
  });

  describe("useTag tests", () => {
    const fetchTag = async () => {
      fetchMock.getOnce("/api/v2/hog/tags/1.0", tagOneDotZero);

      const { result, waitFor } = renderHook(() => useTag(repository, "1.0"), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });

      return result.current;
    };

    it("should return tag", async () => {
      const { data } = await fetchTag();
      expect(data).toEqual(tagOneDotZero);
    });

    it("should cache tag", async () => {
      await fetchTag();

      const cachedTag = queryClient.getQueryData(["repository", "hitchhiker", "heart-of-gold", "tag", "1.0"]);
      expect(cachedTag).toEqual(tagOneDotZero);
    });
  });

  describe("useCreateTags tests", () => {
    const createTag = async () => {
      fetchMock.postOnce("/api/v2/hog/tag", {
        status: 201,
        headers: {
          Location: "/hog/tags/1.0"
        }
      });

      fetchMock.getOnce("/api/v2/hog/tags/1.0", tagOneDotZero);

      const { result, waitForNextUpdate } = renderHook(() => useCreateTag(repository, changeset), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { create } = result.current;
        create("1.0");
        return waitForNextUpdate();
      });

      return result.current;
    };

    const shouldInvalidateQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await createTag();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState!.isInvalidated).toBe(true);
    };

    it("should create tag", async () => {
      const { tag } = await createTag();

      expect(tag).toEqual(tagOneDotZero);
    });

    it("should cache tag", async () => {
      await createTag();

      const cachedTag = queryClient.getQueryData<Tag>(["repository", "hitchhiker", "heart-of-gold", "tag", "1.0"]);
      expect(cachedTag).toEqual(tagOneDotZero);
    });

    it("should invalidate tag collection cache", async () => {
      await shouldInvalidateQuery(["repository", "hitchhiker", "heart-of-gold", "tags"], tags);
    });

    it("should invalidate changeset cache", async () => {
      await shouldInvalidateQuery(["repository", "hitchhiker", "heart-of-gold", "changeset", "42"], changeset);
    });

    it("should invalidate changeset collection cache", async () => {
      await shouldInvalidateQuery(["repository", "hitchhiker", "heart-of-gold", "changesets"], [changeset]);
    });

    it("should fail without location header", async () => {
      fetchMock.postOnce("/api/v2/hog/tag", {
        status: 201
      });

      const { result, waitForNextUpdate } = renderHook(() => useCreateTag(repository, changeset), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { create } = result.current;
        create("awesome-42");
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeDefined();
    });
  });

  describe("useDeleteTags tests", () => {
    const deleteTag = async () => {
      fetchMock.deleteOnce("/api/v2/hog/tags/1.0", {
        status: 204
      });

      const { result, waitForNextUpdate } = renderHook(() => useDeleteTag(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { remove } = result.current;
        remove(tagOneDotZero);
        return waitForNextUpdate();
      });

      return result.current;
    };

    const shouldInvalidateQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await deleteTag();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState!.isInvalidated).toBe(true);
    };

    const shouldRemoveQuery = async (queryKey: string[], data: unknown) => {
      queryClient.setQueryData(queryKey, data);
      await deleteTag();

      const queryState = queryClient.getQueryState(queryKey);
      expect(queryState).toBeUndefined();
    };

    it("should delete tag", async () => {
      const { isDeleted } = await deleteTag();

      expect(isDeleted).toBe(true);
    });

    it("should delete tag cache", async () => {
      await shouldRemoveQuery(["repository", "hitchhiker", "heart-of-gold", "tag", "1.0"], tagOneDotZero);
    });

    it("should invalidate tag collection cache", async () => {
      await shouldInvalidateQuery(["repository", "hitchhiker", "heart-of-gold", "tags"], tags);
    });

    it("should invalidate changeset cache", async () => {
      await shouldInvalidateQuery(["repository", "hitchhiker", "heart-of-gold", "changeset", "42"], changeset);
    });

    it("should invalidate changeset collection cache", async () => {
      await shouldInvalidateQuery(["repository", "hitchhiker", "heart-of-gold", "changesets"], [changeset]);
    });
  });
});

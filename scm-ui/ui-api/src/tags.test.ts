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
 *
 */

import { Changeset, Repository, Tag, TagCollection } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock-jest";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { useCreateTag, useTags } from "./tags";
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
    _links: {}
  };

  const tags: TagCollection = {
    _embedded: {
      tags: [tagOneDotZero]
    },
    _links: {}
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useTags tests", () => {
    it("should return tags", async () => {
      fetchMock.getOnce("/api/v2/hog/tags", tags);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useTags(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });

      expect(result.current.data).toEqual(tags);
    });

    it("should populate tag cache", async () => {
      fetchMock.getOnce("/api/v2/hog/tags", tags);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useTags(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });

      const cachgeTag: Tag | undefined = queryClient.getQueryData([
        "repository",
        "hitchhiker",
        "heart-of-gold",
        "tag",
        "1.0"
      ]);
      expect(cachgeTag?.name).toBe("1.0");
    });
  });

  describe("useCreateTags tests", () => {
    it("should create tag", async () => {
      const queryClient = createInfiniteCachingClient();

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

      expect(result.current.tag).toEqual(tagOneDotZero);
    });

    it("should fail without location header", async () => {
      const queryClient = createInfiniteCachingClient();

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
});

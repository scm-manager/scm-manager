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
import { Branch, BranchCollection, Repository } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock-jest";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { useBranch, useBranches, useCreateBranch, useDeleteBranch } from "./branches";
import { act } from "react-test-renderer";

describe("Test branches hooks", () => {
  const repository: Repository = {
    namespace: "hitchhiker",
    name: "heart-of-gold",
    type: "hg",
    _links: {
      branches: {
        href: "/hog/branches"
      }
    }
  };

  const develop: Branch = {
    name: "develop",
    revision: "42",
    lastCommitter: { name: "trillian" },
    _links: {
      delete: {
        href: "/hog/branches/develop"
      }
    }
  };

  const feature: Branch = {
    name: "feature/something-special",
    revision: "42",
    lastCommitter: { name: "trillian" },
    _links: {
      delete: {
        href: "/hog/branches/feature%2Fsomething-special"
      }
    }
  };

  const branches: BranchCollection = {
    _embedded: {
      branches: [develop]
    },
    _links: {}
  };

  const queryClient = createInfiniteCachingClient();

  beforeEach(() => {
    queryClient.clear();
  });

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useBranches tests", () => {
    const fetchBrances = async () => {
      fetchMock.getOnce("/api/v2/hog/branches", branches);

      const { result, waitFor } = renderHook(() => useBranches(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });

      return result.current.data;
    };

    it("should return branches", async () => {
      const branches = await fetchBrances();
      expect(branches).toEqual(branches);
    });

    it("should add branches to cache", async () => {
      await fetchBrances();

      const data = queryClient.getQueryData<BranchCollection>([
        "repository",
        "hitchhiker",
        "heart-of-gold",
        "branches"
      ]);
      expect(data).toEqual(branches);
    });
  });

  describe("useBranch tests", () => {
    const fetchBranch = async (name: string, branch: Branch) => {
      fetchMock.getOnce("/api/v2/hog/branches/" + encodeURIComponent(name), branch);

      const { result, waitFor } = renderHook(() => useBranch(repository, name), {
        wrapper: createWrapper(undefined, queryClient)
      });

      expect(result.error).toBeUndefined();

      await waitFor(() => {
        return !!result.current.data;
      });

      return result.current.data;
    };

    it("should return branch", async () => {
      const branch = await fetchBranch("develop", develop);
      expect(branch).toEqual(develop);
    });

    it("should escape branch name", async () => {
      const branch = await fetchBranch("feature/something-special", feature);
      expect(branch).toEqual(feature);
    });
  });

  describe("useCreateBranch tests", () => {
    const createBranch = async () => {
      fetchMock.postOnce("/api/v2/hog/branches", {
        status: 201,
        headers: {
          Location: "/hog/branches/develop"
        }
      });

      fetchMock.getOnce("/api/v2/hog/branches/develop", develop);

      const { result, waitForNextUpdate } = renderHook(() => useCreateBranch(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { create } = result.current;
        create({ name: "develop", parent: "main" });
        return waitForNextUpdate();
      });

      return result.current;
    };

    it("should create branch", async () => {
      const { branch } = await createBranch();
      expect(branch).toEqual(develop);
    });

    it("should cache created branch", async () => {
      await createBranch();

      const branch = queryClient.getQueryData<Branch>([
        "repository",
        "hitchhiker",
        "heart-of-gold",
        "branch",
        "develop"
      ]);
      expect(branch).toEqual(develop);
    });

    it("should invalidate cached branches list", async () => {
      queryClient.setQueryData(["repository", "hitchhiker", "heart-of-gold", "branches"], branches);
      await createBranch();

      const queryState = queryClient.getQueryState(["repository", "hitchhiker", "heart-of-gold", "branches"]);
      expect(queryState!.isInvalidated).toBe(true);
    });
  });

  describe("useDeleteBranch tests", () => {
    const deleteBranch = async () => {
      fetchMock.deleteOnce("/api/v2/hog/branches/develop", {
        status: 204
      });

      const { result, waitForNextUpdate } = renderHook(() => useDeleteBranch(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });

      await act(() => {
        const { remove } = result.current;
        remove(develop);
        return waitForNextUpdate();
      });

      return result.current;
    };

    it("should delete branch", async () => {
      const { isDeleted } = await deleteBranch();
      expect(isDeleted).toBe(true);
    });

    it("should delete branch cache", async () => {
      queryClient.setQueryData(["repository", "hitchhiker", "heart-of-gold", "branch", "develop"], develop);
      await deleteBranch();

      const queryState = queryClient.getQueryState(["repository", "hitchhiker", "heart-of-gold", "branch", "develop"]);
      expect(queryState).toBeUndefined();
    });

    it("should invalidate cached branches list", async () => {
      queryClient.setQueryData(["repository", "hitchhiker", "heart-of-gold", "branches"], branches);
      await deleteBranch();

      const queryState = queryClient.getQueryState(["repository", "hitchhiker", "heart-of-gold", "branches"]);
      expect(queryState!.isInvalidated).toBe(true);
    });
  });
});

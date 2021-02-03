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

import { Branch, BranchCollection, Repository } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock-jest";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { useBranches } from "./branches";

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

  const branches: BranchCollection = {
    _embedded: {
      branches: [
        {
          name: "develop",
          revision: "42",
          _links: {}
        }
      ]
    },
    _links: {}
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useBranches tests", () => {
    it("should return branches", async () => {
      fetchMock.getOnce("/api/v2/hog/branches", branches);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useBranches(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });

      expect(result.current.data).toEqual(branches);
    });

    it("should populate branch chache", async () => {
      fetchMock.getOnce("/api/v2/hog/branches", branches);

      const queryClient = createInfiniteCachingClient();

      const { result, waitFor } = renderHook(() => useBranches(repository), {
        wrapper: createWrapper(undefined, queryClient)
      });
      await waitFor(() => {
        return !!result.current.data;
      });

      const cachedBranch: Branch | undefined = queryClient.getQueryData([
        "repository",
        "hitchhiker",
        "heart-of-gold",
        "branch",
        "develop"
      ]);
      expect(cachedBranch?.name).toBe("develop");
    });
  });
});

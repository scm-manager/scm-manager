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

import { Branch, BranchCollection, BranchCreation, Link, Repository } from "@scm-manager/ui-types";
import { requiredLink } from "./links";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { ApiResult } from "./base";
import { branchQueryKey, repoQueryKey } from "./keys";
import { apiClient, urls } from "@scm-manager/ui-components";

export const useBranches = (repository: Repository): ApiResult<BranchCollection> => {
  const link = (repository._links.branches as Link)?.href;
  return useQuery<BranchCollection, Error>(
    repoQueryKey(repository, "branches"),
    () => apiClient.get(link).then(response => response.json()), {
      // we could not throw an error if the link is missing,
      // because some scm systems do not have branches like svn
      enabled: !!link
      // we do not populate the cache for a single branch,
      // because we have no pagination for branches and if we have a lot of them
      // the population slows us down
    }
  );
};

export const useBranch = (repository: Repository, name: string): ApiResult<Branch> => {
  const link = requiredLink(repository, "branches");
  return useQuery<Branch, Error>(branchQueryKey(repository, name), () =>
    apiClient.get(urls.concat(link, name)).then(response => response.json())
  );
};

const createBranch = (link: string) => {
  return (branch: BranchCreation) => {
    return apiClient
      .post(link, branch, "application/vnd.scmm-branchRequest+json;v=2")
      .then(response => {
        const location = response.headers.get("Location");
        if (!location) {
          throw new Error("Server does not return required Location header");
        }
        return apiClient.get(location);
      })
      .then(response => response.json());
  };
};

export const useCreateBranch = (repository: Repository) => {
  const queryClient = useQueryClient();
  const link = requiredLink(repository, "branches");
  const { mutate, isLoading, error, data } = useMutation<Branch, Error, BranchCreation>(createBranch(link), {
    onSuccess: async branch => {
      queryClient.setQueryData(branchQueryKey(repository, branch), branch);
      await queryClient.invalidateQueries(repoQueryKey(repository, "branches"));
    }
  });
  return {
    create: (branch: BranchCreation) => mutate(branch),
    isLoading,
    error,
    branch: data
  };
};

export const useDeleteBranch = (repository: Repository) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Branch>(
    branch => {
      const deleteUrl = (branch._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: async (_, branch) => {
        await queryClient.invalidateQueries(branchQueryKey(repository, branch));
        await queryClient.invalidateQueries(repoQueryKey(repository, "branches"));
      }
    }
  );
  return {
    remove: (branch: Branch) => mutate(branch),
    isLoading,
    error,
    isDeleted: !!data
  };
};

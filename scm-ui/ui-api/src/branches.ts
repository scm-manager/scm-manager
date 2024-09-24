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

import {
  Branch,
  BranchCollection,
  BranchCreation,
  BranchDetails,
  BranchDetailsCollection,
  Link,
  NamespaceAndName,
  Repository
} from "@scm-manager/ui-types";
import { requiredLink } from "./links";
import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from "react-query";
import { ApiResult, ApiResultWithFetching } from "./base";
import { branchQueryKey, repoQueryKey } from "./keys";
import { apiClient } from "./apiclient";
import { concat } from "./urls";
import { useEffect } from "react";

export const useBranches = (repository: Repository): ApiResult<BranchCollection> => {
  const queryClient = useQueryClient();
  const link = requiredLink(repository, "branches");
  return useQuery<BranchCollection, Error>(
    repoQueryKey(repository, "branches"),
    () => apiClient.get(link).then(response => response.json()),
    {
      onSuccess: () => {
        return queryClient.invalidateQueries(branchDetailsQueryKey(repository));
      }
    }
    // we do not populate the cache for a single branch,
    // because we have no pagination for branches and if we have a lot of them
    // the population slows us down
  );
};

export const useBranch = (repository: Repository, name: string): ApiResultWithFetching<Branch> => {
  const link = requiredLink(repository, "branches");
  return useQuery<Branch, Error>(branchQueryKey(repository, name), () =>
    apiClient.get(concat(link, encodeURIComponent(name))).then(response => response.json())
  );
};

function chunkBranches(branches: Branch[]) {
  const chunks: Branch[][] = [];
  const chunkSize = 5;
  let chunkIndex = 0;
  for (const branch of branches) {
    if (!chunks[chunkIndex]) {
      chunks[chunkIndex] = [];
    }
    chunks[chunkIndex].push(branch);
    if (chunks[chunkIndex].length >= chunkSize) {
      chunkIndex = chunkIndex + 1;
    }
  }
  return chunks;
}

const branchDetailsQueryKey = (repository: NamespaceAndName, branch: string | undefined = undefined) => {
  let branchName;
  if (!branch) {
    branchName = "_";
  } else {
    branchName = branch;
  }
  return [...repoQueryKey(repository), "branch-details", branchName];
};

export const useBranchDetailsCollection = (repository: Repository, branches: Branch[]) => {
  const link = requiredLink(repository, "branchDetailsCollection");
  const chunks = chunkBranches(branches);
  const queryClient = useQueryClient();

  const { data, isLoading, error, fetchNextPage } = useInfiniteQuery<
    BranchDetailsCollection,
    Error,
    BranchDetailsCollection
  >(
    branchDetailsQueryKey(repository),
    ({ pageParam = 0 }) => {
      const encodedBranches = chunks[pageParam]?.map(b => encodeURIComponent(b.name)).join("&branches=");
      return apiClient.get(concat(link, `?branches=${encodedBranches}`)).then(response => response.json());
    },
    {
      getNextPageParam: (lastPage, allPages) => {
        if (allPages.length >= chunks.length) {
          return undefined;
        }
        return allPages.length;
      },
      onSuccess: newData => {
        newData.pages
          .flatMap(d => d._embedded?.branchDetails)
          .filter(d => !!d)
          .forEach(d => queryClient.setQueryData(branchDetailsQueryKey(repository, d!.branchName), () => d));
      }
    }
  );

  useEffect(() => {
    fetchNextPage();
  }, [data, fetchNextPage]);

  return {
    data: data?.pages?.map(d => d._embedded?.branchDetails).flat(1),
    isLoading,
    error
  };
};

export const useBranchDetails = (repository: Repository, branch: Branch) => {
  const link = (branch._links.details as Link).href;
  const queryKey = branchDetailsQueryKey(repository, branch.name);
  return useQuery<BranchDetails, Error>(queryKey, () => apiClient.get(link).then(response => response.json()));
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
        queryClient.removeQueries(branchQueryKey(repository, branch));
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

type DefaultBranch = { defaultBranch: string };

export const useDefaultBranch = (repository: Repository): ApiResult<DefaultBranch> => {
  const link = requiredLink(repository, "defaultBranch");
  return useQuery<DefaultBranch, Error>(branchQueryKey(repository, "__default-branch"), () =>
    apiClient.get(link).then(response => response.json())
  );
};

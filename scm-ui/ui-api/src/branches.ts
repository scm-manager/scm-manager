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
  Repository,
} from "@scm-manager/ui-types";
import { objectLink, requiredLink } from "./links";
import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiResult, ApiResultWithFetching } from "./base";
import { branchQueryKey, repoQueryKey } from "./keys";
import { apiClient } from "./apiclient";
import { concat } from "./urls";
import { useEffect } from "react";
import { queryClient } from "./ApiProvider";

export const useBranchesIfAvailable = (repository: Repository): ApiResult<BranchCollection> => {
  const link = objectLink(repository, "branches");
  const result = useQuery<BranchCollection, Error>(
    {
      queryKey: repoQueryKey(repository, "branches"),
      queryFn: () => {
        if (!link) {
          return Promise.resolve({ _embedded: { branches: [] }, _links: {} } as BranchCollection);
        }
        return apiClient.get(link).then((response) => response.json());
      },
    },
    // we do not populate the cache for a single branch,
    // because we have no pagination for branches and if we have a lot of them
    // the population slows us down
  );
  useEffect(() => {
    if (result.data) {
      queryClient.invalidateQueries({ queryKey: branchDetailsQueryKey(repository) });
    }
  }, [repository, result.data]);
  return result;
};

export const useBranches = (repository: Repository): ApiResult<BranchCollection> => {
  const queryClient = useQueryClient();
  const link = requiredLink(repository, "branches");
  const result = useQuery<BranchCollection, Error>({
    queryKey: repoQueryKey(repository, "branches"),
    queryFn: () => apiClient.get(link).then((response) => response.json()),
  });
  useEffect(() => {
    queryClient.invalidateQueries({ queryKey: branchDetailsQueryKey(repository) });
  }, [queryClient, repository, result]);
  // we do not populate the cache for a single branch,
  // because we have no pagination for branches and if we have a lot of them
  // the population slows us down
  return result;
};

export const useBranch = (repository: Repository, name: string): ApiResultWithFetching<Branch> => {
  const link = requiredLink(repository, "branches");
  return useQuery<Branch, Error>({
    queryKey: branchQueryKey(repository, name),
    queryFn: () => apiClient.get(concat(link, encodeURIComponent(name))).then((response) => response.json()),
  });
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
  return [...repoQueryKey(repository), "branch-details", branchName] as string[];
};

type BranchDetailsCollectionData = {
  pages: BranchDetailsCollection[];
};

export const useBranchDetailsCollection = (repository: Repository, branches: Branch[]) => {
  const link = requiredLink(repository, "branchDetailsCollection");
  const chunks = chunkBranches(branches);

  const { data, isLoading, error, fetchNextPage } = useInfiniteQuery<
    BranchDetailsCollection,
    Error,
    BranchDetailsCollectionData,
    string[],
    number
  >({
    queryKey: branchDetailsQueryKey(repository),
    queryFn: ({ pageParam = 0 }) => {
      const encodedBranches = chunks[pageParam]?.map((b) => encodeURIComponent(b.name)).join("&branches=");
      return apiClient.get(concat(link, `?branches=${encodedBranches}`)).then((response) => response.json());
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      if (allPages.length >= chunks.length) {
        return undefined;
      }
      return allPages.length;
    },
  });

  useEffect(() => {
    if (data) {
      data.pages
        .flatMap((d) => d._embedded?.branchDetails)
        .filter((d) => !!d)
        .forEach((d) => queryClient.setQueryData(branchDetailsQueryKey(repository, d!.branchName), () => d));
    }
  }, [data, repository]);

  useEffect(() => {
    fetchNextPage();
  }, [data, fetchNextPage]);

  return {
    data: data?.pages?.map((d) => d._embedded?.branchDetails).flat(1),
    isLoading,
    error,
  };
};

export const useBranchDetails = (repository: Repository, branch: Branch) => {
  const link = (branch._links.details as Link).href;
  const queryKey = branchDetailsQueryKey(repository, branch.name);
  return useQuery<BranchDetails, Error>({
    queryKey: queryKey,
    queryFn: () => apiClient.get(link).then((response) => response.json()),
  });
};

const createBranch = (link: string) => {
  return (branch: BranchCreation) => {
    return apiClient
      .post(link, branch, "application/vnd.scmm-branchRequest+json;v=2")
      .then((response) => {
        const location = response.headers.get("Location");
        if (!location) {
          throw new Error("Server does not return required Location header");
        }
        return apiClient.get(location);
      })
      .then((response) => response.json());
  };
};

export const useCreateBranch = (repository: Repository) => {
  const queryClient = useQueryClient();
  const link = requiredLink(repository, "branches");
  const { mutate, isPending, error, data } = useMutation<Branch, Error, BranchCreation>({
    mutationFn: createBranch(link),
    onSuccess: async (branch) => {
      queryClient.setQueryData(branchQueryKey(repository, branch), branch);
      await queryClient.invalidateQueries({ queryKey: repoQueryKey(repository, "branches") });
    },
  });
  return {
    create: (branch: BranchCreation) => mutate(branch),
    isPending,
    error,
    branch: data,
  };
};

export const useDeleteBranch = (repository: Repository) => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, Branch>({
    mutationFn: (branch) => {
      const deleteUrl = (branch._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    onSuccess: async (_, branch) => {
      queryClient.removeQueries({ queryKey: branchQueryKey(repository, branch) });
      await queryClient.invalidateQueries({ queryKey: repoQueryKey(repository, "branches") });
    },
  });
  return {
    remove: (branch: Branch) => mutate(branch),
    isPending,
    error,
    isDeleted: !!data,
  };
};

type DefaultBranch = { defaultBranch: string };

export const useDefaultBranch = (repository: Repository): ApiResult<DefaultBranch> => {
  const link = requiredLink(repository, "defaultBranch");
  return useQuery<DefaultBranch, Error>({
    queryKey: branchQueryKey(repository, "__default-branch"),
    queryFn: () => apiClient.get(link).then((response) => response.json()),
  });
};

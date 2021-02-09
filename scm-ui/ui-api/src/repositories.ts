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

import {
  Group,
  Link,
  Namespace,
  Repository,
  RepositoryCollection,
  RepositoryCreation,
  RepositoryTypeCollection,
  User
} from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient, urls } from "@scm-manager/ui-components";
import { ApiResult, useIndexJsonResource, useRequiredIndexLink } from "./base";
import { createQueryString } from "./utils";
import { requiredLink } from "./links";
import { repoQueryKey } from "./keys";

export type UseRepositoriesRequest = {
  namespace?: Namespace;
  search?: string;
  page?: number | string;
  disabled?: boolean;
};

export const useRepositories = (request?: UseRepositoriesRequest): ApiResult<RepositoryCollection> => {
  const queryClient = useQueryClient();
  const indexLink = useRequiredIndexLink("repositories");
  const namespaceLink = (request?.namespace?._links.repositories as Link)?.href;
  const link = namespaceLink || indexLink;

  const queryParams: Record<string, string> = {
    sortBy: "namespaceAndName"
  };
  if (request?.search) {
    queryParams.q = request.search;
  }
  if (request?.page) {
    queryParams.page = request.page.toString();
  }
  return useQuery<RepositoryCollection, Error>(
    ["repositories", request?.namespace?.namespace, request?.search || "", request?.page || 0],
    () => apiClient.get(`${link}?${createQueryString(queryParams)}`).then(response => response.json()),
    {
      enabled: !request?.disabled,
      onSuccess: (repositories: RepositoryCollection) => {
        // prepare single repository cache
        repositories._embedded.repositories.forEach((repository: Repository) => {
          queryClient.setQueryData(["repository", repository.namespace, repository.name], repository);
        });
      }
    }
  );
};

type CreateRepositoryRequest = {
  repository: RepositoryCreation;
  initialize: boolean;
};

const createRepository = (link: string) => {
  return (request: CreateRepositoryRequest) => {
    let createLink = link;
    if (request.initialize) {
      createLink += "?initialize=true";
    }
    return apiClient
      .post(createLink, request.repository, "application/vnd.scmm-repository+json;v=2")
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

export const useCreateRepository = () => {
  const queryClient = useQueryClient();
  // not really the index link,
  // but a post to the collection is create by convention
  const link = useRequiredIndexLink("repositories");
  const { mutate, data, isLoading, error } = useMutation<Repository, Error, CreateRepositoryRequest>(
    createRepository(link),
    {
      onSuccess: repository => {
        queryClient.setQueryData(["repository", repository.namespace, repository.name], repository);
        return queryClient.invalidateQueries(["repositories"]);
      }
    }
  );
  return {
    create: (repository: RepositoryCreation, initialize: boolean) => {
      mutate({ repository, initialize });
    },
    isLoading,
    error,
    repository: data
  };
};

// TODO increase staleTime, infinite?
export const useRepositoryTypes = () => {
  const { isLoading, error, data } = useIndexJsonResource<RepositoryTypeCollection>("repositoryTypes");
  return {
    isLoading,
    error,
    repositoryTypes: data?._embedded.repositoryTypes
  };
};

export const useRepository = (namespace: string, name: string): ApiResult<Repository> => {
  const link = useRequiredIndexLink("repositories");
  return useQuery<Repository, Error>(["repository", namespace, name], () =>
    apiClient.get(urls.concat(link, namespace, name)).then(response => response.json())
  );
};

export type UseDeleteRepositoryOptions = {
  onSuccess: (repository: Repository) => void;
};

export const useDeleteRepository = (options?: UseDeleteRepositoryOptions) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Repository>(
    repository => {
      const link = requiredLink(repository, "delete");
      return apiClient.delete(link);
    },
    {
      onSuccess: async (_, repository) => {
        if (options?.onSuccess) {
          options.onSuccess(repository);
        }
        await queryClient.invalidateQueries(repoQueryKey(repository));
        await queryClient.invalidateQueries(["repositories"]);
      }
    }
  );
  return {
    remove: (repository: Repository) => mutate(repository),
    isLoading,
    error,
    isDeleted: !!data
  };
};

export const useUpdateRepository = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Repository>(
    repository => {
      const link = requiredLink(repository, "update");
      return apiClient.put(link, repository, "application/vnd.scmm-repository+json;v=2");
    },
    {
      onSuccess: async (_, repository) => {
        await queryClient.invalidateQueries(repoQueryKey(repository));
        await queryClient.invalidateQueries(["repositories"]);
      }
    }
  );
  return {
    update: (repository: Repository) => mutate(repository),
    isLoading,
    error,
    isUpdated: !!data
  };
};

export const useArchiveRepository = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Repository>(
    repository => {
      const link = requiredLink(repository, "archive");
      return apiClient.post(link);
    },
    {
      onSuccess: async (_, repository) => {
        await queryClient.invalidateQueries(repoQueryKey(repository));
        await queryClient.invalidateQueries(["repositories"]);
      }
    }
  );
  return {
    archive: (repository: Repository) => mutate(repository),
    isLoading,
    error,
    isArchived: !!data
  };
};

export const useUnarchiveRepository = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Repository>(
    repository => {
      const link = requiredLink(repository, "unarchive");
      return apiClient.post(link);
    },
    {
      onSuccess: async (_, repository) => {
        await queryClient.invalidateQueries(repoQueryKey(repository));
        await queryClient.invalidateQueries(["repositories"]);
      }
    }
  );
  return {
    unarchive: (repository: Repository) => mutate(repository),
    isLoading,
    error,
    isUnarchived: !!data
  };
};

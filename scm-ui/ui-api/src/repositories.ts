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
  ExportInfo,
  Link,
  Namespace,
  Paths,
  Repository,
  RepositoryCollection,
  RepositoryCreation,
  RepositoryTypeCollection
} from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { ApiResult, ApiResultWithFetching, useIndexJsonResource, useRequiredIndexLink } from "./base";
import { createQueryString } from "./utils";
import { objectLink, requiredLink } from "./links";
import { repoQueryKey } from "./keys";
import { concat } from "./urls";
import { useEffect, useState } from "react";
import { MissingLinkError, NotFoundError } from "./errors";

export type UseRepositoriesRequest = {
  namespace?: Namespace;
  search?: string;
  page?: number | string;
  disabled?: boolean;
  pageSize?: number;
  showArchived?: boolean;
};

export const useRepositories = (request?: UseRepositoriesRequest): ApiResult<RepositoryCollection> => {
  const queryClient = useQueryClient();
  const indexLink = useRequiredIndexLink("repositories");
  const namespaceLink = (request?.namespace?._links.repositories as Link)?.href;
  const link = namespaceLink || indexLink;

  const queryParams: Record<string, string> = {};
  if (request?.search) {
    queryParams.q = request.search;
  }
  if (request?.pageSize) {
    queryParams.pageSize = request.pageSize.toString();
  }
  if (request?.showArchived !== undefined) {
    queryParams.showArchived = request.showArchived.toString();
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
        repositories._embedded?.repositories.forEach((repository: Repository) => {
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
export const useRepositoryTypes = () => useIndexJsonResource<RepositoryTypeCollection>("repositoryTypes");

export const useRepository = (namespace: string, name: string): ApiResult<Repository> => {
  const link = useRequiredIndexLink("repositories");
  return useQuery<Repository, Error>(["repository", namespace, name], () =>
    apiClient.get(concat(link, namespace, name)).then(response => response.json())
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
        queryClient.removeQueries(repoQueryKey(repository));
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

export const useRunHealthCheck = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Repository>(
    repository => {
      const link = requiredLink(repository, "runHealthCheck");
      return apiClient.post(link);
    },
    {
      onSuccess: async (_, repository) => {
        await queryClient.invalidateQueries(repoQueryKey(repository));
      }
    }
  );
  return {
    runHealthCheck: (repository: Repository) => mutate(repository),
    isLoading,
    error,
    isRunning: !!data
  };
};

export const useExportInfo = (repository: Repository): ApiResultWithFetching<ExportInfo> => {
  const link = requiredLink(repository, "exportInfo");
  //TODO Refetch while exporting to update the page
  const { isLoading, isFetching, error, data } = useQuery<ExportInfo, Error>(
    ["repository", repository.namespace, repository.name, "exportInfo"],
    () => apiClient.get(link).then(response => response.json()),
    {}
  );

  return {
    isLoading,
    isFetching,
    error: error instanceof NotFoundError ? null : error,
    data
  };
};

type ExportOptions = {
  compressed: boolean;
  withMetadata: boolean;
  password?: string;
};

type ExportRepositoryMutateOptions = {
  repository: Repository;
  options: ExportOptions;
};

const EXPORT_MEDIA_TYPE = "application/vnd.scmm-repositoryExport+json;v=2";

export const useExportRepository = () => {
  const queryClient = useQueryClient();
  const [intervalId, setIntervalId] = useState<ReturnType<typeof setTimeout>>();
  useEffect(() => {
    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [intervalId]);
  const { mutate, isLoading, error, data } = useMutation<ExportInfo, Error, ExportRepositoryMutateOptions>(
    ({ repository, options }) => {
      const infolink = requiredLink(repository, "exportInfo");
      let link = requiredLink(repository, options.withMetadata ? "fullExport" : "export");
      if (options.compressed) {
        link += "?compressed=true";
      }
      return apiClient
        .post(link, { password: options.password, async: true }, EXPORT_MEDIA_TYPE)
        .then(() => queryClient.invalidateQueries(repoQueryKey(repository)))
        .then(() => queryClient.invalidateQueries(["repositories"]))
        .then(() => {
          return new Promise<ExportInfo>((resolve, reject) => {
            const id = setInterval(() => {
              apiClient
                .get(infolink)
                .then(r => r.json())
                .then((info: ExportInfo) => {
                  if (info._links.download) {
                    clearInterval(id);
                    resolve(info);
                  }
                })
                .catch(e => {
                  clearInterval(id);
                  reject(e);
                });
            }, 1000);
            setIntervalId(id);
          });
        });
    },
    {
      onSuccess: async (_, { repository }) => {
        await queryClient.invalidateQueries(repoQueryKey(repository));
        await queryClient.invalidateQueries(["repositories"]);
      }
    }
  );
  return {
    exportRepository: (repository: Repository, options: ExportOptions) => mutate({ repository, options }),
    isLoading,
    error,
    data
  };
};

export const usePaths = (repository: Repository, revision: string): ApiResult<Paths> => {
  const link = requiredLink(repository, "paths").replace("{revision}", revision);
  return useQuery<Paths, Error>(repoQueryKey(repository, "paths", revision), () =>
    apiClient.get(link).then(response => response.json())
  );
};

type RenameRepositoryRequest = {
  name: string;
  namespace: string;
};

export const useRenameRepository = (repository: Repository) => {
  const queryClient = useQueryClient();

  const url = objectLink(repository, "renameWithNamespace") || objectLink(repository, "rename");

  if (!url) {
    throw new MissingLinkError(`could not find rename link on repository ${repository.namespace}/${repository.name}`);
  }

  const { mutate, isLoading, error, data } = useMutation<unknown, Error, RenameRepositoryRequest>(
    ({ name, namespace }) => apiClient.post(url, { namespace, name }, "application/vnd.scmm-repository+json;v=2"),
    {
      onSuccess: () => queryClient.removeQueries(repoQueryKey(repository))
    }
  );

  return {
    renameRepository: (namespace: string, name: string) => mutate({ namespace, name }),
    isLoading,
    error,
    isRenamed: !!data
  };
};

export const useReindexRepository = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Repository>(
    (repository) => {
      const link = requiredLink(repository, "reindex");
      return apiClient.post(link);
    },
    {
      onSuccess: async (_, repository) => {
        await queryClient.invalidateQueries(repoQueryKey(repository));
      },
    }
  );
  return {
    reindex: (repository: Repository) => mutate(repository),
    isLoading,
    error,
    isRunning: !!data,
  };
};

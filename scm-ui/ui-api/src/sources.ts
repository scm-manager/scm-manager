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

import { File, Link, Repository } from "@scm-manager/ui-types";
import { requiredLink } from "./links";
import { apiClient } from "./apiclient";
import * as urls from "./urls";
import { useInfiniteQuery } from "react-query";
import { repoQueryKey } from "./keys";
import { useEffect } from "react";
import { createQueryString } from "./utils";

export type UseSourcesOptions = {
  revision?: string;
  path?: string;
  refetchPartialInterval?: number;
  enabled?: boolean;
  collapse?: boolean;
};

const UseSourcesDefaultOptions: UseSourcesOptions = {
  enabled: true,
  refetchPartialInterval: 3000,
  collapse: true
};

export const useSources = (repository: Repository, opts: UseSourcesOptions = UseSourcesDefaultOptions) => {
  const options = {
    ...UseSourcesDefaultOptions,
    ...opts,
  };
  const link = createSourcesLink(repository, options);
  const { isLoading, error, data, isFetchingNextPage, fetchNextPage, refetch } = useInfiniteQuery<File, Error, File>(
    repoQueryKey(repository, "sources", options.revision || "", options.path || "", options.collapse ? "collapse" : ""),
    ({ pageParam }) => {
      return apiClient.get(pageParam || link).then((response) => response.json());
    },
    {
      enabled: options.enabled,
      getNextPageParam: (lastPage) => {
        return (lastPage._links.proceed as Link)?.href;
      },
    }
  );

  const file = merge(data?.pages);
  useEffect(() => {
    const intervalId = setInterval(() => {
      if (isPartial(file)) {
        refetch({
          throwOnError: true,
        });
      }
    }, options.refetchPartialInterval);
    return () => clearInterval(intervalId);
  }, [options.refetchPartialInterval, file, refetch]);

  return {
    isLoading,
    error,
    data: file,
    isFetchingNextPage,
    fetchNextPage: () => {
      // wrapped because we do not want to leak react-query types in our api
      fetchNextPage();
    },
  };
};

const createSourcesLink = (repository: Repository, options: UseSourcesOptions) => {
  let link = requiredLink(repository, "sources");
  if (options.revision) {
    link = urls.concat(link, encodeURIComponent(options.revision));

    if (options.path) {
      link = urls.concat(link, encodeInvalidCharacters(options.path));
    }
  }
  if (options.collapse) {
    return `${link}?${createQueryString({ collapse: "true" })}`;
  }
  return link;
};

const encodeInvalidCharacters = (input: string) => input.replace(/\[/g, "%5B").replace(/]/g, "%5D");

const merge = (files?: File[]): File | undefined => {
  if (!files || files.length === 0) {
    return;
  }
  const children = [];
  for (const page of files) {
    children.push(...(page._embedded?.children || []));
  }
  const lastPage = files[files.length - 1];
  const firstPage = files[0];
  return {
    ...lastPage,
    _embedded: {
      ...lastPage._embedded,
      children,
    },
    _links: {
      ...firstPage._links,
      proceed: lastPage._links.proceed,
    }
  };
};

const isFilePartial = (f: File) => {
  return f.partialResult && !f.computationAborted;
};

const isPartial = (file?: File) => {
  if (!file) {
    return false;
  }
  if (isFilePartial(file)) {
    return true;
  }

  return file._embedded?.children?.some(isFilePartial);
};

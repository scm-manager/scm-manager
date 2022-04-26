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
      link = urls.concat(link, options.path);
    }
  }
  if (options.collapse) {
    return `${link}?${createQueryString({ collapse: "true" })}`;
  }
  return link;
};

const merge = (files?: File[]): File | undefined => {
  if (!files || files.length === 0) {
    return;
  }
  const children = [];
  for (const page of files) {
    children.push(...(page._embedded?.children || []));
  }
  const lastPage = files[files.length - 1];
  return {
    ...lastPage,
    _embedded: {
      ...lastPage._embedded,
      children,
    },
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

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

import parser from "gitdiff-parser";

import { useInfiniteQuery } from "react-query";
import { apiClient } from "./apiclient";
import { Diff, Link } from "@scm-manager/ui-types";

type UseDiffOptions = {
  limit?: number;
  refetchOnWindowFocus?: boolean;
  ignoreWhitespace?: string;
};

const defaultOptions: UseDiffOptions = {
  refetchOnWindowFocus: true,
};

export const useDiff = (link: string, options: UseDiffOptions = defaultOptions) => {
  let initialLink = link;
  if (options.limit) {
    const separator = initialLink.includes("?") ? "&" : "?";
    initialLink = `${initialLink}${separator}limit=${options.limit}&ignoreWhitespace=${options.ignoreWhitespace}`;
  }
  const { isLoading, error, data, isFetchingNextPage, fetchNextPage } = useInfiniteQuery<Diff, Error, Diff>(
    ["link", link, options.ignoreWhitespace],
    ({ pageParam }) => {
      return apiClient.get(pageParam || initialLink).then((response) => {
        const contentType = response.headers.get("Content-Type");
        if (contentType && contentType.toLowerCase() === "application/vnd.scmm-diffparsed+json;v=2") {
          return response.json();
        } else {
          return response
            .text()
            .then(parser.parse)
            .then((parsedGit) => {
              return {
                files: parsedGit,
                partial: false,
                _links: {},
              };
            });
        }
      });
    },
    {
      getNextPageParam: (lastPage) => (lastPage._links.next as Link)?.href,
      refetchOnWindowFocus: options.refetchOnWindowFocus,
    }
  );

  return {
    isLoading,
    error,
    isFetchingNextPage,
    fetchNextPage: () => {
      fetchNextPage();
    },
    data: merge(data?.pages),
  };
};

const merge = (diffs?: Diff[]): Diff | undefined => {
  if (!diffs || diffs.length === 0) {
    return;
  }
  const joinedFiles = diffs.flatMap((diff) => diff.files);
  return {
    ...diffs[diffs.length - 1],
    files: joinedFiles,
  };
};

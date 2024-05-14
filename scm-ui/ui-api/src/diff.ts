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

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

import { ApiResult, ApiResultWithFetching, useIndexJsonResource, useIndexLinks } from "./base";
import { Link, QueryResult, SearchableType } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import { createQueryString } from "./utils";
import { useQueries, useQuery } from "react-query";
import { useEffect, useState } from "react";
import SYNTAX from "./help/search/syntax";
import MODAL from "./help/search/modal";
import { useRepository } from "./repositories";
import { useNamespace } from "./namespaces";

export type SearchOptions = {
  type: string;
  page?: number;
  pageSize?: number;
  namespaceContext?: string;
  repositoryNameContext?: string;
};

type SearchLinks = {
  links: Link[];
  isLoading: boolean;
};

const defaultSearchOptions: SearchOptions = {
  type: "repository",
};

const isString = (str: string | undefined): str is string => !!str;

export const useSearchTypes = (options?: SearchOptions) => {
  const searchLinks = useSearchLinks(options);
  if (searchLinks?.isLoading) {
    return [];
  }
  return searchLinks?.links?.map((link) => link.name).filter(isString) || [];
};

export const useSearchableTypes = () => useIndexJsonResource<SearchableType[]>("searchableTypes");

export const useSearchCounts = (types: string[], query: string, options?: SearchOptions) => {
  const { links, isLoading } = useSearchLinks(options);
  const result: { [type: string]: ApiResultWithFetching<number> } = {};

  const queries = useQueries(
    types.map((type) => ({
      queryKey: ["search", type, query, "count"],
      queryFn: () =>
        apiClient.get(`${findLink(links, type)}?q=${query}&countOnly=true`).then((response) => response.json()),
      enabled: !isLoading,
    }))
  );

  queries.forEach((q, i) => {
    result[types[i]] = {
      isLoading: q.isLoading,
      isFetching: q.isFetching,
      error: q.error as Error,
      data: (q.data as QueryResult)?.totalHits,
    };
  });

  return result;
};

const findLink = (links: Link[], name: string) => {
  for (const l of links) {
    if (l.name === name) {
      return l.href;
    }
  }
  throw new Error(`could not find search link for ${name}`);
};

const useSearchLinks = (options?: SearchOptions): SearchLinks => {
  const links = useIndexLinks();
  const { data: namespace, isLoading: namespaceLoading } = useNamespace(options?.namespaceContext || "");
  const { data: repo, isLoading: repoLoading } = useRepository(
    options?.namespaceContext || "",
    options?.repositoryNameContext || ""
  );

  if (options?.repositoryNameContext) {
    return { links: repo?._links["search"] as Link[], isLoading: repoLoading };
  }

  if (options?.namespaceContext) {
    return { links: namespace?._links["search"] as Link[], isLoading: namespaceLoading };
  }

  const searchLinks = links["search"];
  if (!searchLinks) {
    throw new Error("could not find useInternalSearch links in index");
  }

  if (!Array.isArray(searchLinks)) {
    throw new Error("useInternalSearch links returned in wrong format, array is expected");
  }
  return { links: searchLinks, isLoading: false };
};

const useSearchLink = (options: SearchOptions) => {
  const { links, isLoading } = useSearchLinks(options);
  if (isLoading) {
    return undefined;
  }
  return findLink(links, options.type);
};

export const useOmniSearch = (query: string, optionParam = defaultSearchOptions): ApiResult<QueryResult> => {
  const options = { ...defaultSearchOptions, ...optionParam };
  const link = useSearchLink({ ...options, repositoryNameContext: "", namespaceContext: "" });
  return useInternalSearch(query, options, link);
};

export const useSearch = (query: string, optionParam = defaultSearchOptions): ApiResult<QueryResult> => {
  const options = { ...defaultSearchOptions, ...optionParam };
  const link = useSearchLink(options);

  return useInternalSearch(query, options, link);
};

const useInternalSearch = (query: string, options: SearchOptions, link?: string) => {
  const queryParams: Record<string, string> = {};
  queryParams.q = query;
  if (options.page) {
    queryParams.page = options.page.toString();
  }
  if (options.pageSize) {
    queryParams.pageSize = options.pageSize.toString();
  }
  return useQuery<QueryResult, Error>(
    ["search", options.type, queryParams],
    () => apiClient.get(`${link}?${createQueryString(queryParams)}`).then((response) => response.json()),
    {
      enabled: query?.length > 1 && !!link,
    }
  );
};

const useObserveAsync = <D extends unknown[], R, E = Error>(fn: (...args: D) => Promise<R>, deps: D) => {
  const [data, setData] = useState<R>();
  const [isLoading, setLoading] = useState(false);
  const [error, setError] = useState<E>();
  useEffect(
    () => {
      setLoading(true);
      fn(...deps)
        .then(setData)
        .catch(setError)
        .finally(() => setLoading(false));
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    deps
  );
  return { data, isLoading, error };
};

const getTypedKeys = <K extends string>(input: { readonly [_ in K]: unknown }) => Object.keys(input) as K[];

const isSupportedLanguage = <T>(input: unknown, supportedLanguages: T[]): input is T =>
  supportedLanguages.includes(input as T);

const pickLang = <T>(language: unknown, supportedLanguages: T[], fallback: T): T =>
  isSupportedLanguage(language, supportedLanguages) ? language : fallback;

const SUPPORTED_MODAL_LANGUAGES = getTypedKeys(MODAL);
const SUPPORTED_SYNTAX_LANGUAGES = getTypedKeys(SYNTAX);

const FALLBACK_LANGUAGE = "en";

export const useSearchHelpContent = (language: string) =>
  useObserveAsync(
    (lang) => Promise.resolve(MODAL[pickLang(lang, SUPPORTED_MODAL_LANGUAGES, FALLBACK_LANGUAGE)]),
    [language]
  );

export const useSearchSyntaxContent = (language: string) =>
  useObserveAsync(
    (lang) => Promise.resolve(SYNTAX[pickLang(lang, SUPPORTED_SYNTAX_LANGUAGES, FALLBACK_LANGUAGE)]),
    [language]
  );

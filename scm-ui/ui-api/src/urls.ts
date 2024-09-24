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

import queryString from "query-string";

//@ts-ignore
export const contextPath = window.ctxPath || "";

export function withContextPath(path: string) {
  return contextPath + path;
}

export function withEndingSlash(url: string) {
  if (url.endsWith("/")) {
    return url;
  }
  return url + "/";
}

export function withStartingSlash(url: string) {
  if (url.startsWith("/")) {
    return url;
  }
  return "/" + url;
}

export function concat(base: string, ...parts: string[]) {
  let url = base;
  for (const p of parts) {
    url = withEndingSlash(url) + p;
  }
  return url;
}

export function getNamespaceAndPageFromMatch(match: any) {
  const namespaceFromMatch: string = match.params.namespace;
  const pageFromMatch: string = match.params.page;

  if (!namespaceFromMatch && !pageFromMatch) {
    return { namespace: undefined, page: 1 };
  }

  if (!pageFromMatch) {
    if (namespaceFromMatch.match(/^\d{1,3}$/)) {
      return { namespace: undefined, page: parsePageNumber(namespaceFromMatch) };
    } else {
      return { namespace: namespaceFromMatch, page: 1 };
    }
  }

  return { namespace: namespaceFromMatch, page: parsePageNumber(pageFromMatch) };
}

export function getPageFromMatch(match: any) {
  return parsePageNumber(match.params.page);
}

function parsePageNumber(pageAsString: string) {
  const page = parseInt(pageAsString, 10);
  if (isNaN(page) || !page) {
    return 1;
  }
  return page;
}

export function getQueryStringFromLocation(location: { search?: string }): string | undefined {
  if (location.search) {
    const query = queryString.parse(location.search).q;
    if (query && !Array.isArray(query)) {
      return query;
    }
  }
}

export function getValueStringFromLocationByKey(location: { search?: string }, key: string): string | undefined {
  if (location.search) {
    const value = queryString.parse(location.search)[key];
    if (value && !Array.isArray(value)) {
      return value;
    }
  }
}

export function stripEndingSlash(url: string) {
  if (url.endsWith("/")) {
    return url.substring(0, url.length - 1);
  }
  return url;
}

export function matchedUrlFromMatch(match: any) {
  return stripEndingSlash(match.url);
}

export function matchedUrl(props: any) {
  const match = props.match;
  return matchedUrlFromMatch(match);
}

export function escapeUrlForRoute(url: string) {
  return url.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
}

export function unescapeUrlForRoute(url: string) {
  return url.replace(/\\/g, "");
}

const prevSourcePathQueryName = "prevSourcePath";

export function getPrevSourcePathFromLocation(location: { search?: string }): string | undefined {
  if (location.search) {
    const prevSourcePath = queryString.parse(location.search)[prevSourcePathQueryName];
    if (prevSourcePath && !Array.isArray(prevSourcePath)) {
      return prevSourcePath;
    }
  }
}

export const createPrevSourcePathQuery = (filePath: string) => {
  return filePath ? `${prevSourcePathQueryName}=${encodeURIComponent(filePath)}` : "";
};

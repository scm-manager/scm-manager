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

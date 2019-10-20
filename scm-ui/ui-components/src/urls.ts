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

export function concat(base: string, ...parts: string[]) {
  let url = base;
  for (let p of parts) {
    url = withEndingSlash(url) + p;
  }
  return url;
}

export function getPageFromMatch(match: any) {
  let page = parseInt(match.params.page, 10);
  if (isNaN(page) || !page) {
    page = 1;
  }
  return page;
}

export function getQueryStringFromLocation(location: any) {
  return location.search ? queryString.parse(location.search).q : undefined;
}

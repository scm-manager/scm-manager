// @flow
export const contextPath = window.ctxPath || "";

export function withContextPath(path: string) {
  return contextPath + path;
}

export function getPageFromMatch(match: any) {
  let page = parseInt(match.params.page, 10);
  if (isNaN(page) || !page) {
    page = 1;
  }
  return page;
}

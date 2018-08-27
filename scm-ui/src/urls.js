// @flow
export const contextPath = window.ctxPath || "";

export function withContextPath(path: string) {
  return contextPath + path;
}

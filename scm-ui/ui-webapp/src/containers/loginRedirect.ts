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

/**
 * Builds the redirect target for an unauthenticated request. The originally requested location is
 * preserved as an app-relative `from` query parameter so the user is sent back there after logging
 * in. The router basename (context path) is stripped, because `from` is consumed as an app-relative
 * path by navigate() on the login page (see {@link ./Login#from}).
 *
 * @param requestUrl absolute request url as provided to a react-router loader (includes the basename)
 * @param contextPath the router basename / context path (e.g. "/scm"), may be empty
 */
export const loginRedirectTarget = (requestUrl: string, contextPath: string): string => {
  const url = new URL(requestUrl);
  let from = url.pathname + url.search;
  if (contextPath && from.startsWith(contextPath)) {
    from = from.substring(contextPath.length);
  }
  return from && from !== "/" ? `/login?from=${encodeURIComponent(from)}` : "/login";
};

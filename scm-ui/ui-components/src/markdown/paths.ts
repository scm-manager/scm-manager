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

const externalLinkRegex = new RegExp("^http(s)?://");
export const isExternalLink = (link: string) => {
  return externalLinkRegex.test(link);
};

export const isAnchorLink = (link: string) => {
  return link.startsWith("#");
};

export const isInternalScmRepoLink = (link: string) => {
  return link.startsWith("/repo/");
};

const linkWithProtocolRegex = new RegExp("^([a-z]+):(.+)");
export const isLinkWithProtocol = (link: string) => {
  const match = link.match(linkWithProtocolRegex);
  return match && { protocol: match[1], link: match[2] };
};

export const join = (left: string, right: string) => {
  if (left.endsWith("/") && right.startsWith("/")) {
    return left + right.substring(1);
  } else if (!left.endsWith("/") && !right.startsWith("/")) {
    return left + "/" + right;
  }
  return left + right;
};

export const normalizePath = (path: string) => {
  const stack = [];
  const parts = path.split("/");
  for (const part of parts) {
    if (part === "..") {
      stack.pop();
    } else if (part !== ".") {
      stack.push(part);
    }
  }
  const normalizedPath = stack.join("/");
  if (normalizedPath.startsWith("/")) {
    return normalizedPath.substring(1);
  }
  return normalizedPath;
};

export const isAbsolute = (link: string) => {
  return link.startsWith("/");
};

export const isSubDirectoryOf = (basePath: string, currentPath: string) => {
  return currentPath.startsWith(basePath);
};

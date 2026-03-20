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

export const isLinkWithProtocol = (link: string) => {
  const linkWithProtocolRegex = new RegExp("^([a-z]+):(.+)");
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
      // Go up
      stack.pop();
    } else if (part !== "." && part !== "") {
      // Skip current dir and empty parts
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

export const resolveInternalPath = (currentPath: string, revision: string, link: string) => {
  // Extract path relative to revision
  const pathForMatching = currentPath.replace(encodeURIComponent(revision), revision);
  const revisionWithSlashes = `/${revision}/`;
  const revIndex = pathForMatching.indexOf(revisionWithSlashes);
  let internalPath = "";
  if (revIndex !== -1) {
    internalPath = pathForMatching.substring(revIndex + revisionWithSlashes.length);
  }

  // Determine if path is file or directory
  let directoryPath = internalPath.endsWith("/") ? internalPath.slice(0, -1) : internalPath;
  if (directoryPath.toLowerCase().endsWith(".md")) {
    const parts = directoryPath.split("/");
    parts.pop(); // Removes filename
    directoryPath = parts.join("/");
  }

  // Normalize path
  if (isAbsolute(link)) {
    return normalizePath(link);
  }
  return normalizePath(join(directoryPath, link));
};

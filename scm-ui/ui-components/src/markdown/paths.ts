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

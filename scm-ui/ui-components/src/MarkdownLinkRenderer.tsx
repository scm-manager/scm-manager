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
import React, {FC} from "react";
import {Link, useLocation} from "react-router-dom";
import ExternalLink from "./navigation/ExternalLink";
import {withContextPath} from "./urls";

const externalLinkRegex = new RegExp("^http(s)?://");
export const isExternalLink = (link: string) => {
  return externalLinkRegex.test(link);
};

export const isAnchorLink = (link: string) => {
  return link.startsWith("#");
};

const linkWithProtcolRegex = new RegExp("^[a-z]+:");
export const isLinkWithProtocol = (link: string) => {
  return linkWithProtcolRegex.test(link);
};

const join = (left: string, right: string) => {
  if (left.endsWith("/") && right.startsWith("/")) {
    return left + right.substring(1);
  } else if (!left.endsWith("/") && !right.startsWith("/")) {
    return left + "/" + right;
  }
  return left + right;
};

const normalizePath = (path: string) => {
  const stack = [];
  const parts = path.split("/");
  for (const part of parts) {
    if (part === "..") {
      stack.pop();
    } else if (part !== ".") {
      stack.push(part)
    }
  }
  const normalizedPath = stack.join("/")
  if (normalizedPath.startsWith("/")) {
    return normalizedPath;
  }
  return "/" + normalizedPath;
};

const isAbsolute = (link: string) => {
  return link.startsWith("/");
};

const isSubDirectoryOf = (basePath: string, currentPath: string) => {
  return currentPath.startsWith(basePath);
};

export const createLocalLink = (basePath: string, currentPath: string, link: string) => {
  if (isAbsolute(link)) {
    return join(basePath, link);
  }
  if (!isSubDirectoryOf(basePath, currentPath)) {
    return join(basePath, link);
  }
  let path = currentPath;
  if (currentPath.endsWith("/")) {
    path = currentPath.substring(0, currentPath.length - 2);
  }
  const lastSlash = path.lastIndexOf("/");
  if (lastSlash < 0) {
    path = "";
  } else {
    path = path.substring(0, lastSlash);
  }
  return normalizePath(join(path, link));
};

type LinkProps = {
  href: string;
};

type Props = LinkProps & {
  base: string;
};

const MarkdownLinkRenderer: FC<Props> = ({href, base, children}) => {
  const location = useLocation();
  if (isExternalLink(href)) {
    return <ExternalLink to={href}>{children}</ExternalLink>;
  } else if (isLinkWithProtocol(href)) {
    return <a href={href}>{children}</a>;
  } else if (isAnchorLink(href)) {
    return <a href={withContextPath(location.pathname) + href}>{children}</a>;
  } else {
    const localLink = createLocalLink(base, location.pathname, href);
    return <Link to={localLink}>{children}</Link>;
  }
};

// we use a factory method, because react-markdown does not pass
// base as prop down to our link component.
export const create = (base: string): FC<LinkProps> => {
  return props => <MarkdownLinkRenderer base={base} {...props} />;
};

export default MarkdownLinkRenderer;

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
import React, { FC, ReactNode } from "react";
import { Link, useLocation } from "react-router-dom";
import { withContextPath } from "./urls";

type Props = {
  children: ReactNode;
  href: string;
};

const regex = new RegExp("[a-z]:");

/**
 * Handle local SCM-Manager and external links
 *
 * @VisibleForTesting
 */
export function correctLocalLink(pathname: string, link: string) {
  if (link === "") {
    return pathname;
  }

  // Leave uris unchanged which start with schemes or fragment
  if (link.match(regex) || link.startsWith("#")) {
    return link;
  }

  // Reference to the main directory possible if link starts with slash
  let base = "";
  let path = link;
  if (!link.startsWith("/")) {
    base = pathname;
    // Remove last slash temporary
    if (base.endsWith("/")) base = base.substring(0, base.length - 1);
    // Remove current called file from path
    base = base.substr(0, base.lastIndexOf("/") + 1);

    // Remove first slash for absolute consistence
    if (path.startsWith("/")) path = path.substring(1);
  }

  // Link must end with fragment if it contains one
  const pathParts = path.split("#");
  if (pathParts.length > 1) {
    // Add ending slash in front of fragment
    if (!pathParts[0].endsWith("/")) pathParts[0] += "/";
    path = pathParts[0] + "#" + pathParts[1];
  } else {
    // Add ending slash
    if (!path.endsWith("/")) path += "/";
  }

  return base + path;
}

const MarkdownLinkRenderer: FC<Props> = ({ children, href }) => {
  const location = useLocation();
  const compositeUrl = correctLocalLink(withContextPath(location.pathname), href);

  if (compositeUrl.match(regex)) {
    return <a href={compositeUrl}>{children}</a>;
  }
  return <Link to={compositeUrl}>{children}</Link>;
};

export default MarkdownLinkRenderer;

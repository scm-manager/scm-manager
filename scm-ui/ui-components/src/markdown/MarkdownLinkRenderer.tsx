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
import React, { FC } from "react";
import { Link, useLocation } from "react-router-dom";
import ExternalLink from "../navigation/ExternalLink";
import { urls } from "@scm-manager/ui-api";
import { ProtocolLinkRendererExtensionMap } from "./markdownExtensions";
import {
  isAbsolute, isAnchorLink,
  isExternalLink,
  isInternalScmRepoLink,
  isLinkWithProtocol,
  isSubDirectoryOf,
  join,
  normalizePath
} from "./paths";

export const createLocalLink = (basePath: string, currentPath: string, link: string) => {
  if (isInternalScmRepoLink(link)) {
    return link;
  }
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
  return "/" + normalizePath(join(path, link));
};

type LinkProps = {
  href: string;
};

type Props = LinkProps & {
  base?: string;
};

const MarkdownLinkRenderer: FC<Props> = ({ href = "", base, children, ...props }) => {
  const location = useLocation();
  if (isExternalLink(href)) {
    return <ExternalLink to={href}>{children}</ExternalLink>;
  } else if (isLinkWithProtocol(href)) {
    return <a href={href}>{children}</a>;
  } else if (isAnchorLink(href)) {
    return <a href={urls.withContextPath(location.pathname) + href}>{children}</a>;
  } else if (base) {
    const localLink = createLocalLink(base, location.pathname, href);
    return <Link to={localLink}>{children}</Link>;
  } else if (href) {
    return (
      <a href={href} {...props}>
        {children}
      </a>
    );
  } else {
    return <a {...props}>{children}</a>;
  }
};

// we use a factory method, because react-markdown does not pass
// base as prop down to our link component.
export const create = (base?: string, protocolExtensions: ProtocolLinkRendererExtensionMap = {}): FC<LinkProps> => {
  return (props) => {
    const protocolLinkContext = isLinkWithProtocol(props.href || "");
    if (protocolLinkContext) {
      const { link, protocol } = protocolLinkContext;
      const ProtocolRenderer = protocolExtensions[protocol];
      if (ProtocolRenderer) {
        return (
          <ProtocolRenderer protocol={protocol} href={link}>
            {props.children}
          </ProtocolRenderer>
        );
      }
    }

    return <MarkdownLinkRenderer base={base} {...props} />;
  };
};

export default MarkdownLinkRenderer;

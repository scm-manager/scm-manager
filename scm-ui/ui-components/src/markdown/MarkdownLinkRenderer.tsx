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

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
import { urls, useRepositoryContext, useRepositoryRevisionContext } from "@scm-manager/ui-api";
import { Repository } from "@scm-manager/ui-types";
import { ProtocolLinkRendererExtensionMap } from "./markdownExtensions";
import {
  isAnchorLink,
  isExternalLink,
  isInternalScmRepoLink,
  isLinkWithProtocol,
  join,
  resolveInternalPath,
} from "./paths";

export const createLocalLink = (repository: Repository, revision: string, currentPath: string, link: string) => {
  if (isInternalScmRepoLink(link)) {
    return link;
  }
  const basePath = `/repo/${repository.namespace}/${repository.name}/code/sources/${revision}/`;
  const internalPath = resolveInternalPath(currentPath, revision, link);
  return join(basePath, internalPath);
};

type LinkProps = {
  href: string;
};

type Props = LinkProps & {
  base?: string;
  permalink?: string;
};

const MarkdownLinkRenderer: FC<Props> = ({ href = "", base, children, permalink, ...props }) => {
  const location = useLocation();
  const repository = useRepositoryContext();
  const revision = useRepositoryRevisionContext();
  const pathname = permalink || location.pathname;

  if (isExternalLink(href)) {
    return <ExternalLink to={href}>{children}</ExternalLink>;
  } else if (isLinkWithProtocol(href)) {
    return <a href={href}>{children}</a>;
  } else if (isAnchorLink(href)) {
    return <a href={urls.withContextPath(location.pathname) + href}>{children}</a>;
  } else if (base && repository && revision) {
    const localLink = createLocalLink(repository, revision, pathname, href);
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

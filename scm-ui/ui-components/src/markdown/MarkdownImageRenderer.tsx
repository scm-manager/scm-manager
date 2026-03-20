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
import { useLocation } from "react-router-dom";
import { Link } from "@scm-manager/ui-types";
import { isExternalLink, isInternalScmRepoLink, isLinkWithProtocol, resolveInternalPath } from "./paths";
import { useRepositoryContext, useRepositoryRevisionContext } from "@scm-manager/ui-api";

export const createLocalLink = (contentLink: string, revision: string, currentPath: string, link: string) => {
  if (isInternalScmRepoLink(link)) {
    return link;
  }
  const apiBasePath = contentLink.replace("{revision}", encodeURIComponent(revision));
  const path = resolveInternalPath(currentPath, revision, link);
  return apiBasePath.replace("{path}", path);
};

type LinkProps = {
  src: string;
  alt: string;
};

type Props = LinkProps & {
  base?: string;
  permalink?: string;
  contentLink?: string;
};

const MarkdownImageRenderer: FC<Props> = ({ src = "", alt = "", base, contentLink, children, permalink, ...props }) => {
  const location = useLocation();
  const repository = useRepositoryContext();
  const revision = useRepositoryRevisionContext();
  const pathname = permalink || location.pathname;

  if (isExternalLink(src) || isLinkWithProtocol(src)) {
    return (
      <img src={src} alt={alt}>
        {children}
      </img>
    );
  } else if (base && repository && revision) {
    const localLink = createLocalLink((repository._links.content as Link).href, revision, pathname, src);
    return (
      <img src={localLink} alt={alt}>
        {children}
      </img>
    );
  } else if (src) {
    return (
      <img src={src} alt={alt}>
        {children}
      </img>
    );
  } else {
    return <img {...props}>{children}</img>;
  }
};

// we use a factory method, because react-markdown does not pass
// base as prop down to our link component.
export const create = (base: string | undefined, permalink?: string): FC<LinkProps> => {
  return (props) => {
    return <MarkdownImageRenderer base={base} permalink={permalink} {...props} />;
  };
};

export default MarkdownImageRenderer;

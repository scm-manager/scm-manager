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
import {
  isAbsolute,
  isExternalLink,
  isInternalScmRepoLink,
  isLinkWithProtocol,
  isSubDirectoryOf,
  join,
  normalizePath,
} from "./paths";
import { useRepositoryContext, useRepositoryRevisionContext } from "@scm-manager/ui-api";

export const createLocalLink = (
  basePath: string,
  contentLink: string,
  revision: string,
  currentPath: string,
  link: string
) => {
  const apiBasePath = contentLink.replace("{revision}", encodeURIComponent(revision));
  if (isInternalScmRepoLink(link)) {
    return link;
  }
  if (isAbsolute(link)) {
    return apiBasePath.replace("{path}", link.substring(1));
  }
  const decodedCurrentPath = currentPath.replace(encodeURIComponent(revision), revision);
  if (!isSubDirectoryOf(basePath, decodedCurrentPath)) {
    return apiBasePath.replace("{path}", link);
  }
  const relativePath = decodedCurrentPath.substring(basePath.length);
  let path = relativePath;
  if (decodedCurrentPath.endsWith("/")) {
    path = relativePath.substring(0, relativePath.length - 1);
  }
  const lastSlash = path.lastIndexOf("/");
  if (lastSlash < 0) {
    path = "";
  } else {
    path = path.substring(0, lastSlash);
  }
  return apiBasePath.replace("{path}", normalizePath(join(path, link)));
};

type LinkProps = {
  src: string;
  alt: string;
};

type Props = LinkProps & {
  base?: string;
  contentLink?: string;
};

const MarkdownImageRenderer: FC<Props> = ({ src = "", alt = "", base, contentLink, children, ...props }) => {
  const location = useLocation();
  const repository = useRepositoryContext();
  const revision = useRepositoryRevisionContext();

  if (isExternalLink(src) || isLinkWithProtocol(src)) {
    return (
      <img src={src} alt={alt}>
        {children}
      </img>
    );
  } else if (base && repository && revision) {
    const localLink = createLocalLink(base, (repository._links.content as Link).href, revision, location.pathname, src);
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
export const create = (base: string | undefined): FC<LinkProps> => {
  return (props) => {
    return <MarkdownImageRenderer base={base} {...props} />;
  };
};

export default MarkdownImageRenderer;

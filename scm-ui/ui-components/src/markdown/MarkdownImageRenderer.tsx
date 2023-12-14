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

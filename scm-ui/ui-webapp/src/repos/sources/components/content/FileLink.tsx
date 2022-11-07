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
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { File } from "@scm-manager/ui-types";
import { Tooltip } from "@scm-manager/ui-components";

type Props = {
  baseUrl: string;
  file: File;
  children: ReactNode;
  tabIndex?: number;
};

const isLocalRepository = (repositoryUrl: string) => {
  let host = repositoryUrl.split("/")[2];
  if (host.includes("@")) {
    // remove prefix
    host = host.split("@")[1];
  }
  // remove port
  host = host.split(":")[0];
  // remove query
  host = host.split("?")[0];
  return host === window.location.hostname;
};

export const encodePart = (part: string) => {
  const encoded = encodeURIComponent(part);
  if (part.includes("%")) {
    return encodeURIComponent(encoded);
  }
  return encoded;
};

export const createRelativeLink = (repositoryUrl: string) => {
  const paths = repositoryUrl.split("/");
  return "/" + paths.slice(3).join("/");
};

export const createFolderLink = (base: string, file: File) => {
  let link = base;
  if (file.path) {
    let path = file.path.split("/").map(encodePart).join("/");
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    link += "/" + path;
  }
  if (!link.endsWith("/")) {
    link += "/";
  }
  return link;
};

const FileLink = React.forwardRef<HTMLAnchorElement, Props>(({ baseUrl, file, children, tabIndex }, ref) => {
  const [t] = useTranslation("repos");
  if (file?.subRepository?.repositoryUrl) {
    // file link represents a subRepository
    let link = file.subRepository.repositoryUrl;
    if (file.subRepository.browserUrl) {
      // replace upstream url with public browser url
      link = file.subRepository.browserUrl;
    }
    if (link.startsWith("http://") || link.startsWith("https://")) {
      if (file.subRepository.revision && isLocalRepository(link)) {
        link += "/code/sources/" + file.subRepository.revision;
      }
      return (
        <a href={link} tabIndex={tabIndex}>
          {children}
        </a>
      );
    } else if (link.startsWith("ssh://") && isLocalRepository(link)) {
      link = createRelativeLink(link);
      if (file.subRepository.revision) {
        link += "/code/sources/" + file.subRepository.revision;
      }
      return (
        <Link to={link} tabIndex={tabIndex}>
          {children}
        </Link>
      );
    } else {
      // subRepository url cannot be linked
      return (
        <Tooltip location="top" message={t("sources.fileTree.subRepository") + ": \n" + link}>
          {children}
        </Tooltip>
      );
    }
  }
  // normal file or folder
  return (
    <Link ref={ref} to={createFolderLink(baseUrl, file)} tabIndex={tabIndex}>
      {children}
    </Link>
  );
});

export default FileLink;

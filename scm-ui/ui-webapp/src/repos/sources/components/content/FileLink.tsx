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

import React, { ReactNode } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { File } from "@scm-manager/ui-types";
import { Tooltip, urls } from "@scm-manager/ui-components";

type Props = {
  baseUrl: string;
  file: File;
  children: ReactNode;
  tabIndex?: number;
  repositoryType: string;
};

const getHostname = (repositoryUrl: string) => {
  if (!repositoryUrl.startsWith("http://") && !repositoryUrl.startsWith("https://")) {
    return undefined;
  }
  let host = repositoryUrl.split("/")[2];
  if (host.includes("@")) {
    // remove prefix
    host = host.split("@")[1];
  }
  // remove port
  host = host.split(":")[0];
  // remove query
  host = host.split("?")[0];
  return host;
};

const isLocalRepository = (repositoryUrl: string) => {
  return getHostname(repositoryUrl) === window.location.hostname;
};

export const encodePart = (part: string) => {
  if (part.includes("%")) {
    return encodeURIComponent(part.replace(/%/g, "%25"));
  }
  return encodeURIComponent(part);
};

export const encodeFilePath = (filePath: string) => {
  const encodedUri = encodePart(filePath);
  return encodedUri.replace(/%2F/g, "/");
};

export const createRelativeLink = (
  repositoryUrl: string,
  contextPath: string,
  revision?: string,
  repositoryType?: string
) => {
  const paths = repositoryUrl.split("/");
  const CONTEXT_PART_IN_URL = 3;
  const FOLDER_PART_IN_URL = 7;

  const folder = paths.splice(FOLDER_PART_IN_URL).join("/");
  let url = "/" + paths.slice(CONTEXT_PART_IN_URL, FOLDER_PART_IN_URL).join("/");
  url = url.replace(contextPath, "");
  url += "/code/sources/";
  if (revision) {
    url += revision + "/";
    if (folder !== "") {
      url += folder + "/";
    }
  } else if (repositoryType === "svn" && folder !== "") {
    // type of outgoing repo is svn
    url += `-1/${folder}/`;
  }
  return url;
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

const FileLink = React.forwardRef<HTMLAnchorElement, Props>(
  ({ baseUrl, file, children, tabIndex, repositoryType }, ref) => {
    const [t] = useTranslation("repos");
    if (file?.subRepository?.repositoryUrl) {
      // file link represents a subRepository
      let link = file.subRepository.repositoryUrl;
      if (file.subRepository.browserUrl) {
        // replace upstream url with public browser url
        link = file.subRepository.browserUrl;
      }

      if (isLocalRepository(link)) {
        link = createRelativeLink(link, urls.withContextPath(""), file.subRepository.revision, repositoryType);
        return (
          <Link to={link} tabIndex={tabIndex}>
            {children}
          </Link>
        );
      } else if (link.startsWith("http://") || link.startsWith("https://")) {
        return (
          <a href={link} tabIndex={tabIndex}>
            {children}
          </a>
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
  }
);

export default FileLink;

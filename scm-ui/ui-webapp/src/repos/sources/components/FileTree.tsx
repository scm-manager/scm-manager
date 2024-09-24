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
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { File, Repository } from "@scm-manager/ui-types";
import FileTreeLeaf from "./FileTreeLeaf";
import TruncatedNotification from "./TruncatedNotification";
import { isRootPath } from "../utils/files";
import { KeyboardIterator } from "@scm-manager/ui-shortcuts";

type Props = {
  repository: Repository;
  directory: File;
  baseUrl: string;
  revision: string;
  fetchNextPage: () => void;
  isFetchingNextPage: boolean;
};

const FixedWidthTh = styled.th`
  width: 16px;
`;

export function findParent(path: string) {
  if (path.endsWith("/")) {
    path = path.substring(0, path.length - 1);
  }

  const index = path.lastIndexOf("/");
  if (index > 0) {
    return path.substring(0, index);
  }
  return "";
}

const FileTree: FC<Props> = ({ repository, directory, baseUrl, revision, fetchNextPage, isFetchingNextPage }) => {
  const [t] = useTranslation("repos");
  const { path } = directory;
  const files: File[] = [];

  if (!isRootPath(path)) {
    files.push({
      name: "..",
      path: findParent(path),
      directory: true,
      revision,
      _links: {},
      _embedded: {
        children: [],
      },
    });
  }

  files.push(...(directory._embedded?.children || []));

  const baseUrlWithRevision = baseUrl + "/" + encodeURIComponent(revision);

  const extProps: extensionPoints.ReposSourcesTreeWrapperProps = {
    repository,
    directory,
    baseUrl,
    revision,
  };

  return (
    <div className="panel-block">
      <ExtensionPoint<extensionPoints.ReposSourcesTreeWrapper>
        name="repos.source.tree.wrapper"
        props={extProps}
        renderAll={true}
        wrapper={true}
      >
        <table className="table table-hover table-sm is-fullwidth">
          <thead>
            <tr>
              <FixedWidthTh />
              <th>{t("sources.fileTree.name")}</th>
              <th className="is-hidden-mobile">{t("sources.fileTree.length")}</th>
              <th className="is-hidden-mobile">{t("sources.fileTree.commitDate")}</th>
              <th className="is-hidden-touch">{t("sources.fileTree.description")}</th>
              {binder.hasExtension("repos.sources.tree.row.right") && <th className="is-hidden-mobile" />}
            </tr>
          </thead>
          <tbody>
            <KeyboardIterator>
              {files.map((file: File) => (
                <FileTreeLeaf key={file.name} file={file} baseUrl={baseUrlWithRevision} repository={repository} />
              ))}
            </KeyboardIterator>
          </tbody>
        </table>
        <TruncatedNotification
          directory={directory}
          fetchNextPage={fetchNextPage}
          isFetchingNextPage={isFetchingNextPage}
        />
      </ExtensionPoint>
    </div>
  );
};

export default FileTree;

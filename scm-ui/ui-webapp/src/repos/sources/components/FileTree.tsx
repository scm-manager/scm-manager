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
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { File, Repository } from "@scm-manager/ui-types";
import FileTreeLeaf from "./FileTreeLeaf";
import TruncatedNotification from "./TruncatedNotification";
import { isRootPath } from "../utils/files";
import { ReposSourcesTreeWrapperExtension } from "@scm-manager/ui-extensions/src/extensionPoints";

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
        children: []
      }
    });
  }

  files.push(...(directory._embedded?.children || []));

  const baseUrlWithRevision = baseUrl + "/" + encodeURIComponent(revision);

  const extProps = {
    repository,
    directory,
    baseUrl,
    revision
  };

  return (
    <div className="panel-block">
      <ExtensionPoint<extensionPoints.ReposSourcesTreeWrapperExtension>
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
              {/* TODO Add support for this use-case of extension points */}
              {binder.hasExtension<extensionPoints.ReposSourcesTreeRowRightExtension>(
                "repos.sources.tree.row.right"
              ) && <th className="is-hidden-mobile" />}{" "}
            </tr>
          </thead>
          <tbody>
            {files.map((file: File) => (
              <FileTreeLeaf key={file.name} file={file} baseUrl={baseUrlWithRevision} repository={repository} />
            ))}
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

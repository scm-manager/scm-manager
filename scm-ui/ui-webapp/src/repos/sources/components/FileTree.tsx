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
import { binder } from "@scm-manager/ui-extensions";
import { File } from "@scm-manager/ui-types";
import { Notification } from "@scm-manager/ui-components";
import FileTreeLeaf from "./FileTreeLeaf";
import TruncatedNotification from "./TruncatedNotification";
import {isRootPath} from "../utils/files";

type Props = {
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

const FileTree: FC<Props> = ({ directory, baseUrl, revision, fetchNextPage, isFetchingNextPage }) => {
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

  return (
    <div className="panel-block">
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
          {files.map((file: File) => (
            <FileTreeLeaf key={file.name} file={file} baseUrl={baseUrlWithRevision} />
          ))}
        </tbody>
      </table>
      <TruncatedNotification
        directory={directory}
        fetchNextPage={fetchNextPage}
        isFetchingNextPage={isFetchingNextPage}
      />
    </div>
  );
};

export default FileTree;

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

import React, { FC, useEffect, useState } from "react";
import DiffFile from "./DiffFile";
import { DiffObjectProps, FileControlFactory } from "./DiffTypes";
import { FileDiff } from "@scm-manager/ui-types";
import { getAnchorSelector, getFileNameFromHash } from "./diffs";
import Notification from "../Notification";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import { useScrollToElementWithCallback } from "../useScrollToElement";
import { getAnchorId } from "./diff/helpers";

type Props = DiffObjectProps & {
  diff: FileDiff[];
  fileControlFactory?: FileControlFactory;
  ignoreWhitespace?: string;
  fetchNextPage?: () => void;
  isFetchingNextPage?: boolean;
  isDataPartial?: boolean;
  prevHash?: string;
  setPrevHash?: (newHash: string) => void;
};

const createKey = (file: FileDiff, ignoreWhitespace?: string) => {
  // we need to include the information about hidden whitespace in the key, because otherwise the diff might not be
  // rendered correctly, if the user toggles the ignore whitespace button
  return `${file.oldPath}@${file.oldRevision}/${file.newPath}@${file.newRevision}?${ignoreWhitespace}`;
};

const getFile = (files: FileDiff[] | undefined, path: string): FileDiff | undefined => {
  return files?.find((e) => (e.type !== "delete" && e.newPath === path) || (e.type === "delete" && e.oldPath === path));
};

const selectFromHash = (hash: string) => {
  const fileName = getFileNameFromHash(hash);
  return fileName ? getAnchorSelector(fileName) : undefined;
};

const jumpToBottom = () => {
  window.scrollTo(0, document.body.scrollHeight);
};

const Diff: FC<Props> = ({
  diff,
  ignoreWhitespace,
  fetchNextPage,
  isFetchingNextPage,
  isDataPartial,
  prevHash,
  setPrevHash,
  ...fileProps
}) => {
  const [t] = useTranslation("repos");
  const [contentRef, setContentRef] = useState<HTMLElement | null>();
  const { hash } = useLocation();

  useEffect(() => {
    if (isFetchingNextPage) {
      jumpToBottom();
    }
  }, [isFetchingNextPage]);

  useScrollToElementWithCallback(
    contentRef,
    () => {
      if (isFetchingNextPage === undefined || isDataPartial === undefined || fetchNextPage === undefined) {
        return selectFromHash(hash);
      }

      if (prevHash === hash) {
        return;
      }

      const encodedFileName = getFileNameFromHash(hash);
      if (!encodedFileName) {
        return;
      }

      const selectedFile = getFile(diff, decodeURIComponent(encodedFileName));
      if (selectedFile) {
        return getAnchorSelector(getAnchorId(selectedFile));
      }

      if (isDataPartial && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    [hash, isDataPartial, isFetchingNextPage, fetchNextPage, diff, prevHash, setPrevHash],
    () => {
      if (setPrevHash) {
        setPrevHash(hash);
      }
    }
  );

  return (
    <div ref={setContentRef}>
      {diff.length === 0 ? (
        <Notification type="info">{t("diff.noDiffFound")}</Notification>
      ) : (
        diff.map((file) => <DiffFile key={createKey(file, ignoreWhitespace)} file={file} {...fileProps} />)
      )}
    </div>
  );
};

Diff.defaultProps = {
  sideBySide: false,
};

export default Diff;

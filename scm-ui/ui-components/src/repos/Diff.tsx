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

import React, { FC, useState } from "react";
import DiffFile from "./DiffFile";
import { DiffObjectProps, FileControlFactory } from "./DiffTypes";
import { FileDiff } from "@scm-manager/ui-types";
import { escapeWhitespace } from "./diffs";
import Notification from "../Notification";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import useScrollToElement from "../useScrollToElement";

type Props = DiffObjectProps & {
  diff: FileDiff[];
  fileControlFactory?: FileControlFactory;
  ignoreWhitespace?: string;
};

const createKey = (file: FileDiff, ignoreWhitespace?: string) => {
  // we need to include the information about hidden whitespace in the key, because otherwise the diff might not be
  // rendered correctly, if the user toggles the ignore whitespace button
  return `${file.oldPath}@${file.oldRevision}/${file.newPath}@${file.newRevision}?${ignoreWhitespace}`;
};

const getAnchorSelector = (uriHashContent: string) => {
  return "#" + escapeWhitespace(decodeURIComponent(uriHashContent));
};

const Diff: FC<Props> = ({ diff, ignoreWhitespace, ...fileProps }) => {
  const [t] = useTranslation("repos");
  const [contentRef, setContentRef] = useState<HTMLElement | null>();
  const { hash } = useLocation();
  useScrollToElement(
    contentRef,
    () => {
      const match = hash.match(/^#diff-(.*)$/);
      if (match) {
        return getAnchorSelector(match[1]);
      }
    },
    hash
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

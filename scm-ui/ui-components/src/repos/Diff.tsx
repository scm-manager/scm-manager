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

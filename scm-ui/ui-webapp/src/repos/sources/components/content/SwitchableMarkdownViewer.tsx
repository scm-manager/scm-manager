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
import MarkdownViewer from "./MarkdownViewer";
import { File } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, SyntaxHighlighter } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useFileContent } from "@scm-manager/ui-api";
import replaceBranchWithRevision from "../../ReplaceBranchWithRevision";
import { useLocation } from "react-router-dom";
import classNames from "classnames";

type Props = {
  file: File;
  basePath: string;
};

const SwitchableMarkdownViewer: FC<Props> = ({ file, basePath }) => {
  const { isLoading, error, data: content } = useFileContent(file);
  const { t } = useTranslation("repos");
  const location = useLocation();
  const [renderMarkdown, setRenderMarkdown] = useState(true);

  const toggleMarkdown = () => {
    setRenderMarkdown(!renderMarkdown);
  };

  if (isLoading) {
    return <Loading />;
  }
  if (error) {
    return <ErrorNotification error={error} />;
  }

  const permalink = replaceBranchWithRevision(location.pathname, file.revision);

  return (
    <div className="is-relative">
      <div className="tabs is-toggle is-right">
        <ul>
          <li className={classNames({ "is-active": renderMarkdown })} onClick={toggleMarkdown}>
            <button>{t("sources.content.toggleButton.showMarkdown")}</button>
          </li>
          <li className={classNames({ "is-active": !renderMarkdown })} onClick={toggleMarkdown}>
            <button>{t("sources.content.toggleButton.showSources")}</button>
          </li>
        </ul>
      </div>
      {renderMarkdown ? (
        <MarkdownViewer content={content || ""} basePath={basePath} permalink={permalink} />
      ) : (
        <SyntaxHighlighter language="markdown" value={content || ""} permalink={permalink} />
      )}
    </div>
  );
};

export default SwitchableMarkdownViewer;

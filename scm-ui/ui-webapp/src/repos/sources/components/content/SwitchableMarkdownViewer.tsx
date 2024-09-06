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
import MarkdownViewer from "./MarkdownViewer";
import { File, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, SyntaxHighlighter } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useFileContent } from "@scm-manager/ui-api";
import replaceBranchWithRevision from "../../ReplaceBranchWithRevision";
import { useLocation } from "react-router-dom";
import classNames from "classnames";

type Props = {
  file: File;
  basePath: string;
  repository: Repository;
};

const SwitchableMarkdownViewer: FC<Props> = ({ file, basePath, repository }) => {
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
            <a>{t("sources.content.toggleButton.showMarkdown")}</a>
          </li>
          <li className={classNames({ "is-active": !renderMarkdown })} onClick={toggleMarkdown}>
            <a>{t("sources.content.toggleButton.showSources")}</a>
          </li>
        </ul>
      </div>
      {renderMarkdown ? (
        <MarkdownViewer
          content={content || ""}
          basePath={basePath}
          permalink={permalink}
          revision={file.revision}
          repository={repository}
        />
      ) : (
        <SyntaxHighlighter language="markdown" value={content || ""} permalink={permalink} />
      )}
    </div>
  );
};

export default SwitchableMarkdownViewer;

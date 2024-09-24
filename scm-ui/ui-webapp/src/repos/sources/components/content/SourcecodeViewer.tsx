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
import { ErrorNotification, Loading, SyntaxHighlighter } from "@scm-manager/ui-components";
import { File } from "@scm-manager/ui-types";
import { useLocation } from "react-router-dom";
import replaceBranchWithRevision from "../../ReplaceBranchWithRevision";
import { useFileContent } from "@scm-manager/ui-api";

type Props = {
  file: File;
  language: string;
};

const SourcecodeViewer: FC<Props> = ({ file, language }) => {
  const { error, isLoading, data: content } = useFileContent(file);
  const location = useLocation();

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  const permalink = replaceBranchWithRevision(location.pathname, file.revision);

  return <SyntaxHighlighter language={language.toLowerCase()} value={content || ""} permalink={permalink} />;
};

export default SourcecodeViewer;

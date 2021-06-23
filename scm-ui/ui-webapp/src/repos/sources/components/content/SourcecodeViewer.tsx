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

  if (!content || isLoading) {
    return <Loading />;
  }

  const permalink = replaceBranchWithRevision(location.pathname, file.revision);

  return <SyntaxHighlighter language={language.toLowerCase()} value={content} permalink={permalink} />;
};

export default SourcecodeViewer;

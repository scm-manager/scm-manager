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
import { File } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, MarkdownView } from "@scm-manager/ui-components";
import replaceBranchWithRevision from "../../ReplaceBranchWithRevision";
import { useLocation } from "react-router-dom";
import { useFileContent } from "@scm-manager/ui-api";

type Props = {
  file: File;
  basePath: string;
};

const MarkdownViewer: FC<Props> = ({ file, basePath }) => {
  const { isLoading, error, data: content } = useFileContent(file);
  const location = useLocation();

  if (!content || isLoading) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  const permalink = replaceBranchWithRevision(location.pathname, file.revision);

  return (
    <div className="p-2">
      <MarkdownView content={content} basePath={basePath} permalink={permalink} enableAnchorHeadings={true} />
    </div>
  );
};

export default MarkdownViewer;

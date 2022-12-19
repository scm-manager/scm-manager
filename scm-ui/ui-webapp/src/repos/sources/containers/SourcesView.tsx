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
import SourcecodeViewer from "../components/content/SourcecodeViewer";
import ImageViewer from "../components/content/ImageViewer";
import DownloadViewer from "../components/content/DownloadViewer";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { File, Link, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, PdfViewer } from "@scm-manager/ui-components";
import SwitchableMarkdownViewer from "../components/content/SwitchableMarkdownViewer";
import styled from "styled-components";
import { useContentType } from "@scm-manager/ui-api";
import { determineSyntaxHighlightingLanguage } from "../utils/files";

const NoSpacingSyntaxHighlighterContainer = styled.div`
  & pre {
    margin: 0 !important;
    padding: 0 0 0.5rem !important;
  }
`;

type Props = {
  repository: Repository;
  file: File;
  revision: string;
};

const SourcesView: FC<Props> = ({ file, repository, revision }) => {
  const { data: contentTypeData, error, isLoading } = useContentType((file._links.self as Link).href);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!contentTypeData || isLoading) {
    return <Loading />;
  }

  let sources;

  const language = determineSyntaxHighlightingLanguage(contentTypeData);
  const contentType = contentTypeData.type;
  const basePath = `/repo/${repository.namespace}/${repository.name}/code/sources/${revision}/`;
  if (contentType.startsWith("image/")) {
    sources = <ImageViewer file={file} />;
  } else if (contentType.includes("markdown") || (language && language.toLowerCase() === "markdown")) {
    sources = <SwitchableMarkdownViewer file={file} basePath={basePath} repository={repository} />;
  } else if (language) {
    sources = <SourcecodeViewer file={file} language={language} />;
  } else if (contentType.startsWith("text/")) {
    sources = <SourcecodeViewer file={file} language="none" />;
  } else if (contentType.startsWith("application/pdf")) {
    sources = <PdfViewer src={file} />;
  } else {
    sources = (
      <ExtensionPoint<extensionPoints.RepositorySourcesView>
        name="repos.sources.view"
        props={{
          file,
          contentType,
          revision,
          basePath,
        }}
      >
        <DownloadViewer repository={repository} file={file} />
      </ExtensionPoint>
    );
  }

  return <NoSpacingSyntaxHighlighterContainer className="panel-block">{sources}</NoSpacingSyntaxHighlighterContainer>;
};

export default SourcesView;

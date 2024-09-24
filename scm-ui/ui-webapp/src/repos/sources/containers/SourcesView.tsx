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

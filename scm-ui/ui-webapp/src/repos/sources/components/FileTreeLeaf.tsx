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

import * as React from "react";
import { FC, ReactElement } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { File, Repository } from "@scm-manager/ui-types";
import { DateFromNow, devices, FileSize, Icon, Tooltip } from "@scm-manager/ui-components";
import FileIcon from "./FileIcon";
import FileLink from "./content/FileLink";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

type Props = WithTranslation & {
  repository: Repository;
  file: File;
  baseUrl: string;
};

const MinWidthTd = styled.td`
  min-width: 10em;
`;

const NoWrapTd = styled.td`
  white-space: nowrap;
`;

const ExtensionTd = styled.td`
  white-space: break-spaces;
  text-align: right;

  > *:not(:last-child) {
    margin-right: 0.5rem;
  }
  @media screen and (min-width: ${devices.widescreen.width}px) {
    white-space: nowrap;
  }
`;

const FileName: FC<{ file: File; baseUrl: string; repositoryType: string }> = ({ file, baseUrl, repositoryType }) => {
  const ref = useKeyboardIteratorTarget();
  return (
    <FileLink ref={ref} baseUrl={baseUrl} file={file} tabIndex={0} repositoryType={repositoryType}>
      {file.name}
    </FileLink>
  );
};

class FileTreeLeaf extends React.Component<Props> {
  createFileIcon = (file: File, repositoryType: string) => {
    return (
      <FileLink baseUrl={this.props.baseUrl} file={file} tabIndex={-1} repositoryType={repositoryType}>
        <FileIcon file={file} />
      </FileLink>
    );
  };

  contentIfPresent = (file: File, attribute: string, content: (file: File) => ReactElement | string | undefined) => {
    const { t } = this.props;
    if (file.hasOwnProperty(attribute)) {
      return content(file);
    } else if (file.computationAborted) {
      return (
        <Tooltip location="top" message={t("sources.fileTree.computationAborted")}>
          <Icon name="question-circle" alt={t("sources.fileTree.computationAborted")} />
        </Tooltip>
      );
    } else if (file.partialResult) {
      return (
        <Tooltip location="top" message={t("sources.fileTree.notYetComputed")}>
          <Icon name="hourglass" alt={t("sources.fileTree.notYetComputed")} />
        </Tooltip>
      );
    } else {
      return content(file);
    }
  };

  render() {
    const { repository, file, baseUrl } = this.props;

    const renderFileSize = (file: File) => <FileSize bytes={file?.length ? file.length : 0} />;
    const renderCommitDate = (file: File) => <DateFromNow date={file.commitDate} />;

    const extProps: extensionPoints.ReposSourcesTreeRowProps = {
      repository,
      file,
    };

    return (
      <>
        <tr>
          <td>{this.createFileIcon(file, repository.type)}</td>
          <MinWidthTd className="is-word-break">
            <FileName file={file} baseUrl={baseUrl} repositoryType={repository.type} />
          </MinWidthTd>
          <NoWrapTd className="is-hidden-mobile">
            {file.directory ? "" : this.contentIfPresent(file, "length", renderFileSize)}
          </NoWrapTd>
          <td className="is-hidden-mobile">{this.contentIfPresent(file, "commitDate", renderCommitDate)}</td>
          <MinWidthTd className={classNames("is-word-break", "is-hidden-touch")}>
            {this.contentIfPresent(file, "description", (file) => file.description)}
          </MinWidthTd>

          {binder.hasExtension<extensionPoints.ReposSourcesTreeRowRight>("repos.sources.tree.row.right", extProps) && (
            <ExtensionTd className="is-hidden-mobile">
              {!file.directory && (
                <ExtensionPoint<extensionPoints.ReposSourcesTreeRowRight>
                  name="repos.sources.tree.row.right"
                  props={extProps}
                  renderAll={true}
                />
              )}
            </ExtensionTd>
          )}
        </tr>
        <ExtensionPoint<extensionPoints.ReposSourcesTreeRowAfter>
          name="repos.sources.tree.row.after"
          props={extProps}
          renderAll={true}
        />
      </>
    );
  }
}

export default withTranslation("repos")(FileTreeLeaf);

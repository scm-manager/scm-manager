import * as React from "react";
import { Link } from "react-router-dom";
import classNames from "classnames";
import styled from "styled-components";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { File } from "@scm-manager/ui-types";
import { DateFromNow, FileSize, Tooltip } from "@scm-manager/ui-components";
import FileIcon from "./FileIcon";
import { Icon } from "@scm-manager/ui-components/src";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  file: File;
  baseUrl: string;
};

const MinWidthTd = styled.td`
  min-width: 10em;
`;

const NoWrapTd = styled.td`
  white-space: nowrap;
`;

export function createLink(base: string, file: File) {
  let link = base;
  if (file.path) {
    let path = file.path;
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    link += "/" + path;
  }
  if (!link.endsWith("/")) {
    link += "/";
  }
  return link;
}

class FileTreeLeaf extends React.Component<Props> {
  createLink = (file: File) => {
    return createLink(this.props.baseUrl, file);
  };

  createFileIcon = (file: File) => {
    if (file.directory) {
      return (
        <Link to={this.createLink(file)}>
          <FileIcon file={file} />
        </Link>
      );
    }
    return (
      <Link to={this.createLink(file)}>
        <FileIcon file={file} />
      </Link>
    );
  };

  createFileName = (file: File) => {
    if (file.directory) {
      return <Link to={this.createLink(file)}>{file.name}</Link>;
    }
    return <Link to={this.createLink(file)}>{file.name}</Link>;
  };

  contentIfPresent = (file: File, attribute: string, content: (file: File) => any) => {
    const { t } = this.props;
    if (file.hasOwnProperty(attribute)) {
      return content(file);
    } else if (file.computationAborted) {
      return (
        <Tooltip location="top" message={t("sources.file-tree.computationAborted")}>
          <Icon name={"question-circle"} />
        </Tooltip>
      );
    } else if (file.partialResult) {
      return (
        <Tooltip location="top" message={t("sources.file-tree.notYetComputed")}>
          <Icon name={"hourglass"} />
        </Tooltip>
      );
    } else {
      return content(file);
    }
  };

  render() {
    const { file } = this.props;

    const renderFileSize = (file: File) => <FileSize bytes={file?.length ? file.length : 0} />;
    const renderCommitDate = (file: File) => <DateFromNow date={file.commitDate} />;

    return (
      <tr>
        <td>{this.createFileIcon(file)}</td>
        <MinWidthTd className="is-word-break">{this.createFileName(file)}</MinWidthTd>
        <NoWrapTd className="is-hidden-mobile">
          {file.directory ? "" : this.contentIfPresent(file, "length", renderFileSize)}
        </NoWrapTd>
        <td className="is-hidden-mobile">{this.contentIfPresent(file, "commitDate", renderCommitDate)}</td>
        <MinWidthTd className={classNames("is-word-break", "is-hidden-touch")}>
          {this.contentIfPresent(file, "description", file => file.description)}
        </MinWidthTd>
        {binder.hasExtension("repos.sources.tree.row.right") && (
          <td className="is-hidden-mobile">
            {!file.directory && (
              <ExtensionPoint
                name="repos.sources.tree.row.right"
                props={{
                  file
                }}
                renderAll={true}
              />
            )}
          </td>
        )}
      </tr>
    );
  }
}

export default withTranslation("repos")(FileTreeLeaf);

//@flow
import * as React from "react";
import { Link } from "react-router-dom";
import classNames from "classnames";
import styled from "styled-components";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import type { File } from "@scm-manager/ui-types";
import { DateFromNow, FileSize } from "@scm-manager/ui-components";
import FileIcon from "./FileIcon";

type Props = {
  file: File,
  baseUrl: string
};

const MinWidthTd = styled.td`
  min-width: 10em;
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

export default class FileTreeLeaf extends React.Component<Props> {
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

  render() {
    const { file } = this.props;

    const fileSize = file.directory ? "" : <FileSize bytes={file.length} />;

    return (
      <tr>
        <td>{this.createFileIcon(file)}</td>
        <MinWidthTd className="is-word-break">
          {this.createFileName(file)}
        </MinWidthTd>
        <td className="is-hidden-mobile">{fileSize}</td>
        <td className="is-hidden-mobile">
          <DateFromNow date={file.lastModified} />
        </td>
        <MinWidthTd className={classNames("is-word-break", "is-hidden-mobile")}>
          {file.description}
        </MinWidthTd>
        {binder.hasExtension("repos.sources.tree.row.right") && (
          <td className="is-hidden-mobile">
            {!file.directory && (
              <ExtensionPoint
                name="repos.sources.tree.row.right"
                props={{ file }}
                renderAll={true}
              />
            )}
          </td>
        )}
      </tr>
    );
  }
}

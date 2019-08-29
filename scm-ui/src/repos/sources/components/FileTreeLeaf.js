//@flow
import * as React from "react";
import injectSheet from "react-jss";
import { DateFromNow } from "@scm-manager/ui-components";
import FileSize from "./FileSize";
import FileIcon from "./FileIcon";
import { Link } from "react-router-dom";
import type { File } from "@scm-manager/ui-types";
import classNames from "classnames";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";

const styles = {
  iconColumn: {
    width: "16px"
  },
  wordBreakMinWidth: {
    minWidth: "10em"
  }
};

type Props = {
  file: File,
  baseUrl: string,

  // context props
  classes: any
};

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

  render() {
    const { file, classes } = this.props;

    const fileSize = file.directory ? "" : <FileSize bytes={file.length} />;

    return (
      <tr>
        <td className={classes.iconColumn}>{this.createFileIcon(file)}</td>
        <td className={classNames(classes.wordBreakMinWidth, "is-word-break")}>
          {this.createFileName(file)}
        </td>
        <td className="is-hidden-mobile">{fileSize}</td>
        <td className="is-hidden-mobile">
          <DateFromNow date={file.lastModified} />
        </td>
        <td
          className={classNames(
            classes.wordBreakMinWidth,
            "is-word-break",
            "is-hidden-mobile"
          )}
        >
          {file.description}
        </td>
        {binder.hasExtension("repos.sources.tree.row.right") && (
          <td>
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

export default injectSheet(styles)(FileTreeLeaf);

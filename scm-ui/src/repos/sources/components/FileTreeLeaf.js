//@flow
import * as React from "react";
import injectSheet from "react-jss";
import { DateFromNow } from "@scm-manager/ui-components";
import FileSize from "./FileSize";
import FileIcon from "./FileIcon";
import { Link } from "react-router-dom";
import type { File } from "@scm-manager/ui-types";

const styles = {
  iconColumn: {
    width: "16px"
  }
};

type Props = {
  file: File,
  baseUrl: string,

  // context props
  classes: any
};

class FileTreeLeaf extends React.Component<Props> {
  createLink = (file: File) => {
    let link = this.props.baseUrl;
    if (file.path) {
      link += "/" + file.path + "/";
    }
    return link;
  };

  createFileIcon = (file: File) => {
    if (file.directory) {
      return (
        <Link to={this.createLink(file)}>
          <FileIcon file={file} />
        </Link>
      );
    }
    return <FileIcon file={file} />;
  };

  createFileName = (file: File) => {
    if (file.directory) {
      return <Link to={this.createLink(file)}>{file.name}</Link>;
    }
    return file.name;
  };

  render() {
    const { file, classes } = this.props;

    return (
      <tr>
        <td className={classes.iconColumn}>{this.createFileIcon(file)}</td>
        <td>{this.createFileName(file)}</td>
        <td>
          <FileSize bytes={file.length} />
        </td>
        <td>
          <DateFromNow date={file.lastModified} />
        </td>
        <td>{file.description}</td>
      </tr>
    );
  }
}

export default injectSheet(styles)(FileTreeLeaf);

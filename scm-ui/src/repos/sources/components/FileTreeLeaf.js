//@flow
import React from "react";
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

  // context props
  classes: any
};

class FileTreeLeaf extends React.Component<Props> {
  render() {
    const { file, classes } = this.props;

    return (
      <tr>
        <td className={classes.iconColumn}>
          <Link to="#todo">
            <FileIcon file={file} />
          </Link>
        </td>
        <td>
          <Link to="#todo">{file.name}</Link>
        </td>
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

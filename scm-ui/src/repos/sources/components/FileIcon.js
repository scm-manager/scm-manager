// @flow
import React from "react";
import type { File } from "@scm-manager/ui-types";

type Props = {
  file: File
};

class FileIcon extends React.Component<Props> {
  render() {
    const { file } = this.props;
    let icon = "file";
    if (file.directory) {
      icon = "folder";
    }
    return <i className={`fa fa-${icon}`} />;
  }
}

export default FileIcon;

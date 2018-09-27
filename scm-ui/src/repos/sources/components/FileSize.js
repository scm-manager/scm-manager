// @flow
import React from "react";

type Props = {
  bytes: number
};

class FileSize extends React.Component<Props> {
  static format(bytes) {
    if (!bytes) {
      return "";
    }

    const units = ["B", "K", "M", "G", "T", "P", "E", "Z", "Y"];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));

    const size = (bytes / 1024 ** i).toFixed(2);
    return `${size} ${units[i]}`;
  }

  render() {
    const formattedBytes = FileSize.format(this.props.bytes);
    return <span>{formattedBytes}</span>;
  }
}

export default FileSize;

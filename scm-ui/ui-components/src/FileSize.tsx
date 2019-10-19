import React from 'react';

type Props = {
  bytes: number;
};

class FileSize extends React.Component<Props> {
  static format(bytes: number) {
    if (!bytes) {
      return '0 B';
    }

    const units = ['B', 'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'];
    const i = Math.floor(Math.log(bytes) / Math.log(1000));

    const size = i === 0 ? bytes : (bytes / 1000 ** i).toFixed(2);
    return `${size} ${units[i]}`;
  }

  render() {
    const formattedBytes = FileSize.format(this.props.bytes);
    return <span>{formattedBytes}</span>;
  }
}

export default FileSize;

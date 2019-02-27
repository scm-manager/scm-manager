//@flow
import React from "react";
import DiffFile from "./DiffFile";
import type { DiffObjectProps } from "./DiffTypes";

type Props = DiffObjectProps & {
  diff: any
};

class Diff extends React.Component<Props> {
  static defaultProps = {
    sideBySide: false
  };

  render() {
    const { diff, ...fileProps } = this.props;
    return (
      <>
        {diff.map((file, index) => (
          <DiffFile key={index} file={file} {...fileProps} />
        ))}
      </>
    );
  }
}

export default Diff;

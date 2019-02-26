//@flow
import React from "react";
import DiffFile from "./DiffFile";

type Props = {
  diff: any,
  sideBySide: boolean
};

class Diff extends React.Component<Props> {

  static defaultProps = {
    sideBySide: false
  };

  renderFile = (file: any, i: number) => {
    const { sideBySide } = this.props;
    return <DiffFile key={i} file={file} sideBySide={sideBySide} />;
  };

  render() {
    const { diff } = this.props;
    return (
      <>
        {diff.map(this.renderFile)}
      </>
    );
  }

}

export default Diff;

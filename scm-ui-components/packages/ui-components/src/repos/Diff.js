//@flow
import React from "react";
import { Diff2Html } from "diff2html";

type Props = {
  diff: string,
  sideBySide: boolean
};

class Diff extends React.Component<Props> {

  static defaultProps = {
    sideBySide: false
  };

  render() {
    const { diff, sideBySide } = this.props;

    const options = {
      inputFormat: "diff",
      outputFormat: sideBySide ? "side-by-side" : "line-by-line",
      showFiles: false,
      matching: "lines"
    };

    const outputHtml = Diff2Html.getPrettyHtml(diff, options);

    return (
      // eslint-disable-next-line react/no-danger
      <div dangerouslySetInnerHTML={{ __html: outputHtml }} />
    );
  }

}

export default Diff;

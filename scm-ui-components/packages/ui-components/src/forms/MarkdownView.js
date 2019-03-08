//@flow
import React from "react";

type Props = {
  content: string
};

class MarkdownView extends React.Component<Props> {

  render() {
    const {content } = this.props;
    return (
      <div>
        {content}
      </div>
    );
  }
}

export default MarkdownView;

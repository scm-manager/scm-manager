// @flow
import React from "react";
import ReactSyntaxHighlighter from "react-syntax-highlighter";
import { arduinoLight } from "react-syntax-highlighter/dist/styles/hljs";

type Props = {
  language: string,
  value: string
};

class SyntaxHighlighter extends React.Component<Props> {

  render() {
    return (
      <ReactSyntaxHighlighter
        showLineNumbers="false"
        language={this.props.language}
        style={arduinoLight}
      >
        {this.props.value}
      </ReactSyntaxHighlighter>
    );
  }
}

export default SyntaxHighlighter;

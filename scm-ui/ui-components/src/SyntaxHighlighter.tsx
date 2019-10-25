import React from "react";

import { LightAsync as ReactSyntaxHighlighter } from "react-syntax-highlighter";
// @ts-ignore
import { arduinoLight } from "react-syntax-highlighter/dist/cjs/styles/hljs";

type Props = {
  language: string;
  value: string;
};

class SyntaxHighlighter extends React.Component<Props> {
  render() {
    return (
      <ReactSyntaxHighlighter showLineNumbers={false} language={this.props.language} style={arduinoLight}>
        {this.props.value}
      </ReactSyntaxHighlighter>
    );
  }
}

export default SyntaxHighlighter;

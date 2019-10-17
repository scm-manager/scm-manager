// @flow
import React from "react";

// TODO fix code splitting in production
// Storybook does not like the LightAsync import:
// import { LightAsync as ReactSyntaxHighlighter } from "react-syntax-highlighter";
// so we should use the default import for development and the LightAsync for production
import ReactSyntaxHighlighter from "react-syntax-highlighter";
import { arduinoLight } from "react-syntax-highlighter/dist/cjs/styles/hljs";

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

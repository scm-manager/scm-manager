import React from "react";

import { LightAsync as ReactSyntaxHighlighter } from "react-syntax-highlighter";
// @ts-ignore
import { arduinoLight } from "react-syntax-highlighter/dist/cjs/styles/hljs";

type Props = {
  language?: string;
  value: string;
};

const defaultLanguage = "text";

class SyntaxHighlighter extends React.Component<Props> {
  static defaultProps: Partial<Props> = {
    language: defaultLanguage
  };

  getLanguage = () => {
    const { language } = this.props;
    if (language) {
      return language;
    }
    return defaultLanguage;
  };

  render() {
    const language = this.getLanguage();
    return (
      <ReactSyntaxHighlighter showLineNumbers={false} language={language} style={arduinoLight}>
        {this.props.value}
      </ReactSyntaxHighlighter>
    );
  }
}

export default SyntaxHighlighter;

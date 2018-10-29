// @flow
import React from "react";
import { translate } from "react-i18next";
import { apiClient } from "@scm-manager/ui-components";
import type { File } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import SyntaxHighlighter from "react-syntax-highlighter";
import { arduinoLight } from "react-syntax-highlighter/styles/hljs";

type Props = {
  t: string => string,
  file: File,
  contentType: string
};

type State = {
  content: string,
  error: Error,
  hasError: boolean,
  loaded: boolean
};

class SourcecodeViewer extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      content: "",
      error: new Error(),
      hasError: false,
      loaded: false
    };
  }

  componentDidMount() {
    const { file } = this.props;
    getContent(file._links.self.href)
      .then(result => {
        if (result.error) {
          this.setState({
            ...this.state,
            hasError: true,
            error: result.error,
            loaded: true
          });
        } else {
          this.setState({
            ...this.state,
            content: result,
            loaded: true
          });
        }
      })
      .catch(err => {});
  }

  render() {
    const content = this.state.content;
    const error = this.state.error;
    const hasError = this.state.hasError;
    const loaded = this.state.loaded;
    const { contentType } = this.props;

    if (hasError) {
      return <ErrorNotification error={error} />;
    }

    if (!loaded) {
      return <Loading />;
    }

    if (!content) {
      return null;
    }

    return (
      <SyntaxHighlighter
        showLineNumbers="true"
        language={getLanguage(contentType)}
        style={arduinoLight}
      >
        {content}
      </SyntaxHighlighter>
    );
  }
}

export function getLanguage(contentType: string) {
  return contentType.substring(
    contentType.indexOf("/") + 1,
    contentType.length
  );
}

export function getContent(url: string) {
  return apiClient
    .get(url)
    .then(response => response.text())
    .then(content => {
      return content;
    })
    .catch(err => {
      return { error: err };
    });
}

export default translate("repos")(SourcecodeViewer);

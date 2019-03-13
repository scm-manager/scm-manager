// @flow
import React from "react";
import { translate } from "react-i18next";
import { apiClient, SyntaxHighlighter } from "@scm-manager/ui-components";
import type { File } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  file: File,
  language: string
};

type State = {
  content: string,
  error?: Error,
  loaded: boolean
};

class SourcecodeViewer extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      content: "",
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
    const { content, error, loaded } = this.state;
    const language = this.props.language;

    if (error) {
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
        language={getLanguage(language)}
        value= {content}
      />
    );
  }
}

export function getLanguage(language: string) {
  return language.toLowerCase();
}

export function getContent(url: string) {
  return apiClient
    .get(url)
    .then(response => response.text())
    .then(response => {
      return response;
    })
    .catch(err => {
      return { error: err };
    });
}

export default translate("repos")(SourcecodeViewer);

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { apiClient, ErrorNotification, Loading, SyntaxHighlighter } from "@scm-manager/ui-components";
import { File } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  file: File;
  language: string;
};

type State = {
  content: string;
  error?: Error;
  loaded: boolean;
  currentFileRevision: string;
};

class SourcecodeViewer extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      content: "",
      loaded: false,
      currentFileRevision: ""
    };
  }

  componentDidMount() {
    const { file } = this.props;
    const { currentFileRevision } = this.state;
    if (file.revision !== currentFileRevision) {
      this.fetchContent();
    }
  }

  componentDidUpdate() {
    const { file } = this.props;
    const { currentFileRevision } = this.state;
    if (file.revision !== currentFileRevision) {
      this.fetchContent();
    }
  }

  fetchContent = () => {
    const { file } = this.props;
    getContent(file._links.self.href)
      .then(content => {
        this.setState({
          content,
          loaded: true,
          currentFileRevision: file.revision
        });
      })
      .catch(error => {
        this.setState({
          error,
          loaded: true
        });
      });
  };

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

    return <SyntaxHighlighter language={getLanguage(language)} value={content} />;
  }
}

export function getLanguage(language: string) {
  return language.toLowerCase();
}

export function getContent(url: string) {
  return apiClient.get(url).then(response => response.text());
}

export default withTranslation("repos")(SourcecodeViewer);

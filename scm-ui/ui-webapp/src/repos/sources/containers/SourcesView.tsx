import React from "react";
import styled from "styled-components";
import { WithTranslation, withTranslation } from "react-i18next";
import SourcecodeViewer from "../components/content/SourcecodeViewer";
import ImageViewer from "../components/content/ImageViewer";
import MarkdownViewer from "../components/content/MarkdownViewer";
import DownloadViewer from "../components/content/DownloadViewer";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { getContentType } from "./contentType";
import { File, Repository } from "@scm-manager/ui-types";
import { Button, ErrorNotification, Level, Loading } from "@scm-manager/ui-components";

const ToggleButton = styled(Button)`
  max-width: 2em;
  margin-right: 0.25em;
`;

type Props = WithTranslation & {
  repository: Repository;
  file: File;
  revision: string;
  path: string;
};

type State = {
  contentType: string;
  language: string;
  renderMarkdown: boolean;
  loaded: boolean;
  error?: Error;
};

class SourcesView extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      contentType: "",
      language: "",
      loaded: false,
      renderMarkdown: true
    };
  }

  componentDidMount() {
    const { file } = this.props;
    getContentType(file._links.self.href)
      .then(result => {
        this.setState({
          ...this.state,
          contentType: result.type,
          language: result.language,
          loaded: true
        });
      })
      .catch(error => {
        this.setState({
          ...this.state,
          error,
          loaded: true
        });
      });
  }

  toggleMarkdown = () => {
    this.setState({ renderMarkdown: !this.state.renderMarkdown });
  };

  showSources() {
    const { file, revision } = this.props;
    const { contentType, language, renderMarkdown } = this.state;
    if (contentType.startsWith("image/")) {
      return <ImageViewer file={file} />;
    } else if (contentType.includes("markdown")) {
      return (
        <>
          <Level
            right={
              <ToggleButton
                color={renderMarkdown ? "" : "primary"}
                action={this.toggleMarkdown}
                title={renderMarkdown ? "render sources" : "render markdown"}
              >
                <i className="fab fa-markdown"></i>
              </ToggleButton>
            }
          />
          {renderMarkdown ? <MarkdownViewer file={file} /> : <SourcecodeViewer file={file} language={language} />}
        </>
      );
    } else if (language) {
      return <SourcecodeViewer file={file} language={language} />;
    } else if (contentType.startsWith("text/")) {
      return <SourcecodeViewer file={file} language="none" />;
    } else {
      return (
        <ExtensionPoint
          name="repos.sources.view"
          props={{
            file,
            contentType,
            revision
          }}
        >
          <DownloadViewer file={file} />
        </ExtensionPoint>
      );
    }
  }

  render() {
    const { file } = this.props;
    const { loaded, error } = this.state;

    if (!file || !loaded) {
      return <Loading />;
    }
    if (error) {
      return <ErrorNotification error={error} />;
    }

    const sources = this.showSources();

    return <div className="panel-block">{sources}</div>;
  }
}

export default withTranslation("repos")(SourcesView);

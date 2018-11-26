// @flow
import React from "react";
import { translate } from "react-i18next";

import SourcecodeViewer from "../components/content/SourcecodeViewer";
import ImageViewer from "../components/content/ImageViewer";
import DownloadViewer from "../components/content/DownloadViewer";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { getContentType } from "./contentType";
import type { File, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  file: File,
  revision: string,
  path: string,
  classes: any,
  t: string => string
};

type State = {
  contentType: string,
  language: string,
  loaded: boolean,
  error?: Error
};

class SourcesView extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      contentType: "",
      language: "",
      loaded: false
    };
  }

  componentDidMount() {
    const { file } = this.props;
    getContentType(file._links.self.href)
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
            contentType: result.type,
            language: result.language,
            loaded: true
          });
        }
      })
      .catch(err => {});
  }

  showSources() {
    const { file, revision } = this.props;
    const { contentType, language } = this.state;
    if (contentType.startsWith("image/")) {
      return <ImageViewer file={file} />;
    } else if (language) {
      return <SourcecodeViewer file={file} language={language} />;
    } else if (contentType.startsWith("text/")) {
      return <SourcecodeViewer file={file} language="none" />;
    } else {
      return (
        <ExtensionPoint
          name="repos.sources.view"
          props={{ file, contentType, revision }}
        >
          <DownloadViewer file={file} />
        </ExtensionPoint>
      );
    }
  }

  render() {
    const { classes, file } = this.props;
    const { loaded, error } = this.state;

    if (!file || !loaded) {
      return <Loading />;
    }
    if (error) {
      return <ErrorNotification error={error} />;
    }

    const sources = this.showSources();

    return <>{sources}</>;
  }
}

export default translate("repos")(SourcesView);

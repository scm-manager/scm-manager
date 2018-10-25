// @flow
import React from "react";
import { translate } from "react-i18next";
import { apiClient } from "@scm-manager/ui-components";
import { getSources } from "../modules/sources";
import type {
  Repository,
  File
} from "@scm-manager/ui-types";
import {
  ErrorNotification,
  Loading
} from "@scm-manager/ui-components";
import { connect } from "react-redux";
import ImageViewer from "../components/content/ImageViewer";
import SourcecodeViewer from "../components/content/SourcecodeViewer";
import DownloadViewer from "../components/content/DownloadViewer";

type Props = {
  t: string => string,
  loading: boolean,
  error: Error,
  file: File,
  repository: Repository,
  revision: string,
  path: string,
  // context props
  classes: any,
  t: string => string,
  match: any
};

type State = {
  contentType: string,
  error: Error,
  hasError: boolean
};

class Content extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      contentType: "",
      error: new Error(),
      hasError: false
    };
  }

  componentDidMount() {
    const { file } = this.props;
    getContentType(file._links.self.href)
      .then(result => {
        if (result.error) {
          this.setState({
            ...this.state,
            hasError: true,
            error: result.error
          });
        } else {
          this.setState({
            ...this.state,
            contentType: result.type
          });
        }
      })
      .catch(err => {});
  }

  render() {
    const { file } = this.props;
    const contentType = this.state.contentType;
    const error = this.state.error;
    const hasError = this.state.hasError;

    if (!file) {
      return <Loading />;
    }
    if (hasError) {
      return <ErrorNotification error={error} />;
    }
    if (contentType.startsWith("image")) {
      return <ImageViewer />;
    }

    if (contentType.startsWith("text")) {
      return <SourcecodeViewer />;
    }

    return <DownloadViewer file={file}/>;
  }
}

export function getContentType(url: string, state: any) {
  return apiClient
    .head(url)
    .then(response => {
      return { type: response.headers.get("Content-Type") };
    })
    .catch(err => {
      return { error: err };
    });
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, revision, path } = ownProps;

  const file = getSources(state, repository, revision, path);

  return {
    file
  };
};

export default connect(mapStateToProps)(translate("repos")(Content));

// @flow
import React from "react";
import { translate } from "react-i18next";
import { apiClient } from "../../../../../scm-ui-components/packages/ui-components/src/index";
import { getSources } from "../modules/sources";
import type {
  Repository,
  File
} from "../../../../../scm-ui-components/packages/ui-types/src/index";
import {
  ErrorNotification,
  Loading
} from "../../../../../scm-ui-components/packages/ui-components/src/index";
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
  contentType: string
};

class Content extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      contentType: ""
    };
  }

  componentDidMount() {
    const { file } = this.props;
    getContentType(file._links.self.href).then(result => {
      this.setState({
        contentType: result
      });
    });
  }

  render() {
    const { file } = this.props;
    const contentType = this.state.contentType;
    if (!file) {
      return <Loading />;
    }
    if (contentType.startsWith("image")) {
      return <ImageViewer />;
    }

    if (contentType.startsWith("text")) {
      return <SourcecodeViewer />;
    }

    return <DownloadViewer />;
  }
}

export function getContentType(url: string, state: any) {
  return apiClient
    .head(url)
    .then(response => {
      return response.headers.get("Content-Type");
    })
    .catch(err => {
      return null;
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

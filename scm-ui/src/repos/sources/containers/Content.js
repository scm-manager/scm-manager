// @flow
import React from "react";
import { Interpolate, translate } from "react-i18next";
import { apiClient } from "@scm-manager/ui-components";
import { getSources } from "../modules/sources";
import type { Repository, File } from "@scm-manager/ui-types";
import {
  ErrorNotification,
  Loading,
  DateFromNow
} from "@scm-manager/ui-components";
import { connect } from "react-redux";
import ImageViewer from "../components/content/ImageViewer";
import SourcecodeViewer from "../components/content/SourcecodeViewer";
import DownloadViewer from "../components/content/DownloadViewer";
import FileSize from "../components/FileSize";
import AvatarWrapper from "../../components/changesets/AvatarWrapper";
import classNames from "classnames";
import AvatarImage from "../../components/changesets/AvatarImage";
import ChangesetAuthor from "../../components/changesets/ChangesetAuthor";

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

  showHeader() {
    const { file, revision, t } = this.props;
    const date = <DateFromNow date={file.lastModified} />;
    const fileSize = file.directory ? "" : <FileSize bytes={file.length} />;

    return (
      <div className="content">
        <h4>{file.name}</h4>
        <article className="media">
          <div className="media-content">
            <p>
              {file.description.split("\n").map((item, key) => {
                return (
                  <span key={key}>
                    {item}
                    <br />
                  </span>
                );
              })}
            </p>
          </div>
          <div className="media-right">{date}</div>
        </article>
      </div>
    );
  }

  showContent() {
    const { file, revision } = this.props;
    const contentType = this.state.contentType;
    if (contentType.startsWith("image")) {
      return <ImageViewer />;
    } else if (contentType.startsWith("text")) {
      return <SourcecodeViewer />;
    } else {
      return <DownloadViewer file={file} revision={revision} />;
    }
  }

  render() {
    const { file } = this.props;
    const error = this.state.error;
    const hasError = this.state.hasError;

    if (!file) {
      return <Loading />;
    }
    if (hasError) {
      return <ErrorNotification error={error} />;
    }

    const header = this.showHeader();
    const content = this.showContent();
    const fileSize = file.directory ? "" : <FileSize bytes={file.length} />;

    return (
      <div>
        {header}
        <nav className="panel">
          <article className="panel-heading media">
            <div className="media-content">{file.name}</div>
            <div className="media-right">{fileSize}</div>
          </article>
          <div className="panel-block">{content}</div>
        </nav>
      </div>
    );
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

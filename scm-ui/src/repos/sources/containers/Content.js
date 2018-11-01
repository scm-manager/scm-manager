// @flow
import React from "react";
import { translate } from "react-i18next";
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
import injectSheet from "react-jss";
import classNames from "classnames";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { getContentType } from "./contentType";

type Props = {
  loading: boolean,
  error: Error,
  file: File,
  repository: Repository,
  revision: string,
  path: string,
  classes: any,
  t: string => string
};

type State = {
  contentType: string,
  language: string,
  error: Error,
  hasError: boolean,
  loaded: boolean,
  collapsed: boolean
};

const styles = {
  toCenterContent: {
    display: "block"
  },
  pointer: {
    cursor: "pointer"
  }
};

class Content extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      contentType: "",
      language: "",
      error: new Error(),
      hasError: false,
      loaded: false,
      collapsed: true
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

  toggleCollapse = () => {
    this.setState(prevState => ({
      collapsed: !prevState.collapsed
    }));
  };

  showHeader() {
    const { file, classes } = this.props;
    const collapsed = this.state.collapsed;
    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";
    const fileSize = file.directory ? "" : <FileSize bytes={file.length} />;

    return (
      <span className={classes.pointer} onClick={this.toggleCollapse}>
        <article className="media">
          <div className="media-left">
            <i className={classNames("fa", icon)} />
          </div>
          <div className="media-content">
            <div className="content">{file.name}</div>
          </div>
          <p className="media-right">{fileSize}</p>
        </article>
      </span>
    );
  }

  showMoreInformation() {
    const collapsed = this.state.collapsed;
    const { classes, file, revision } = this.props;
    const date = <DateFromNow date={file.lastModified} />;
    const description = file.description ? (
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
    ) : null;
    if (!collapsed) {
      return (
        <div className={classNames("panel-block", classes.toCenterContent)}>
          <table className="table">
            <tbody>
              <tr>
                <th>Path</th>
                <th>{file.path}</th>
              </tr>
              <tr>
                <th>Branch</th>
                <th>{revision}</th>
              </tr>
              <tr>
                <th>Last modified</th>
                <th>{date}</th>
              </tr>
              <tr>
                <th>Description</th>
                <th>{description}</th>
              </tr>
            </tbody>
          </table>
        </div>
      );
    }
    return null;
  }

  showContent() {
    const { file, revision } = this.props;
    const contentType = this.state.contentType;
    const language = this.state.language;
    if (contentType.startsWith("image/")) {
      return <ImageViewer file={file} />;
    } else if (language) {
      return <SourcecodeViewer file={file} language={language} />;
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
    const { file, classes } = this.props;
    const error = this.state.error;
    const hasError = this.state.hasError;
    const loaded = this.state.loaded;

    if (!file || !loaded) {
      return <Loading />;
    }
    if (hasError) {
      return <ErrorNotification error={error} />;
    }

    const header = this.showHeader();
    const content = this.showContent();
    const moreInformation = this.showMoreInformation();

    return (
      <div>
        <nav className="panel">
          <article className="panel-heading">{header}</article>
          {moreInformation}
          <div className={classNames("panel-block", classes.toCenterContent)}>
            {content}
          </div>
        </nav>
      </div>
    );
  }
}


const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, revision, path } = ownProps;

  const file = getSources(state, repository, revision, path);

  return {
    file
  };
};

export default injectSheet(styles)(
  connect(mapStateToProps)(translate("repos")(Content))
);

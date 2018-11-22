// @flow
import React from "react";
import { translate } from "react-i18next";
import { getSources } from "../modules/sources";
import type { File, Repository } from "@scm-manager/ui-types";
import {
  DateFromNow,
  ErrorNotification,
  Loading
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
  loaded: boolean,
  collapsed: boolean,
  error?: Error
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
    const { file, classes, t } = this.props;
    const collapsed = this.state.collapsed;
    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";

    return (
      <span className={classes.pointer} onClick={this.toggleCollapse}>
        <article className="media">
          <div className="media-left">
            <i className={classNames("fa", icon)}/>
          </div>
          <div className="media-content">
            <div className="content">{file.name}</div>
          </div>
          <p className="media-right">
            <a className="is-hidden-mobile" href="#">
              <span className="icon is-medium">
              <i className="fas fa-history"></i>
                </span>

              {t("sources.content.historyLink")}
            </a>
          </p>
        </article>
      </span>
    );
  }

  showMoreInformation() {
    const collapsed = this.state.collapsed;
    const { classes, file, revision, t } = this.props;
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
    const fileSize = file.directory ? "" : <FileSize bytes={file.length} />;
    if (!collapsed) {
      return (
        <div className={classNames("panel-block", classes.toCenterContent)}>
          <table className="table">
            <tbody>
              <tr>
                <td>{t("sources.content.path")}</td>
                <td>{file.path}</td>
              </tr>
              <tr>
                <td>{t("sources.content.branch")}</td>
                <td>{revision}</td>
              </tr>
              <tr>
                <td>{t("sources.content.size")}</td>
                <td>{fileSize}</td>
              </tr>
              <tr>
                <td>{t("sources.content.lastModified")}</td>
                <td>{date}</td>
              </tr>
              <tr>
                <td>{t("sources.content.description")}</td>
                <td>{description}</td>
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
    const { file, classes } = this.props;
    const { loaded, error } = this.state;

    if (!file || !loaded) {
      return <Loading />;
    }
    if (error) {
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

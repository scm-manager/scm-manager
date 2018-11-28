// @flow
import React from "react";
import { translate } from "react-i18next";
import type { File, Repository } from "@scm-manager/ui-types";
import { DateFromNow } from "@scm-manager/ui-components";
import FileSize from "../components/FileSize";
import injectSheet from "react-jss";
import classNames from "classnames";
import ButtonGroup from "../components/content/ButtonGroup";
import SourcesView from "./SourcesView";
import HistoryView from "./HistoryView";
import { getSources } from "../modules/sources";
import { connect } from "react-redux";

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
  collapsed: boolean,
  showHistory: boolean
};

const styles = {
  toCenterContent: {
    display: "block"
  },
  pointer: {
    cursor: "pointer"
  },
  marginInHeader: {
    marginRight: "0.5em"
  },
  isVerticalCenter: {
    display: "flex",
    alignItems: "center"
  }
};

class Content extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      collapsed: true,
      showHistory: false
    };
  }

  toggleCollapse = () => {
    this.setState(prevState => ({
      collapsed: !prevState.collapsed
    }));
  };

  setShowHistoryState(showHistory: boolean) {
    this.setState({
      ...this.state,
      showHistory
    });
  }

  showHeader() {
    const { file, classes } = this.props;
    const { showHistory, collapsed } = this.state;
    const icon = collapsed ? "fa-angle-right" : "fa-angle-down";

    const selector = file._links.history ? (
      <ButtonGroup
        file={file}
        historyIsSelected={showHistory}
        showHistory={(changeShowHistory: boolean) =>
          this.setShowHistoryState(changeShowHistory)
        }
      />
    ) : null;

    return (
      <span className={classes.pointer}>
        <article className={classNames("media", classes.isVerticalCenter)}>
          <div className="media-content" onClick={this.toggleCollapse}>
            <i
              className={classNames(
                "fa is-medium",
                icon,
                classes.marginInHeader
              )}
            />
            <span>{file.name}</span>
          </div>
          <div className="media-right">{selector}</div>
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

  render() {
    const { file, revision, repository, path, classes } = this.props;
    const { showHistory } = this.state;

    const header = this.showHeader();
    const content =
      showHistory && file._links.history ? (
        <HistoryView file={file} repository={repository} />
      ) : (
        <SourcesView
          revision={revision}
          file={file}
          repository={repository}
          path={path}
        />
      );
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

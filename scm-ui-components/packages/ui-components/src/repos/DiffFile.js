//@flow
import React from "react";
import {
  Hunk,
  Diff as DiffComponent,
  getChangeKey,
  Change,
  DiffObjectProps,
  File
} from "react-diff-view";
import injectSheets from "react-jss";
import classNames from "classnames";
import { translate } from "react-i18next";

const styles = {
  panel: {
    fontSize: "1rem"
  },
  titleHeader: {
    cursor: "pointer"
  },
  title: {
    marginLeft: ".25rem",
    fontSize: "1rem"
  },
  hunkDivider: {
    margin: ".5rem 0"
  },
  changeType: {
    marginLeft: ".75rem"
  }
};

type Props = DiffObjectProps & {
  file: File,
  // context props
  classes: any,
  t: string => string
};

type State = {
  collapsed: boolean
};

class DiffFile extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: false
    };
  }

  toggleCollapse = () => {
    this.setState(state => ({
      collapsed: !state.collapsed
    }));
  };

  setCollapse = (collapsed: boolean) => {
    this.setState({
      collapsed
    });
  };

  createHunkHeader = (hunk: Hunk, i: number) => {
    const { classes } = this.props;
    if (i > 0) {
      return <hr className={classes.hunkDivider} />;
    }
    return null;
  };

  collectHunkAnnotations = (hunk: Hunk) => {
    const { annotationFactory, file } = this.props;
    if (annotationFactory) {
      return annotationFactory({
        hunk,
        file
      });
    }
  };

  handleClickEvent = (change: Change, hunk: Hunk) => {
    const { file, onClick } = this.props;
    const context = {
      changeId: getChangeKey(change),
      change,
      hunk,
      file
    };
    if (onClick) {
      onClick(context);
    }
  };

  createCustomEvents = (hunk: Hunk) => {
    const { onClick } = this.props;
    if (onClick) {
      return {
        gutter: {
          onClick: (change: Change) => {
            this.handleClickEvent(change, hunk);
          }
        }
      };
    }
  };

  renderHunk = (hunk: Hunk, i: number) => {
    return (
      <Hunk
        key={hunk.content}
        hunk={hunk}
        header={this.createHunkHeader(hunk, i)}
        widgets={this.collectHunkAnnotations(hunk)}
        customEvents={this.createCustomEvents(hunk)}
      />
    );
  };

  renderFileTitle = (file: any) => {
    if (
      file.oldPath !== file.newPath &&
      (file.type === "copy" || file.type === "rename")
    ) {
      return (
        <>
          {file.oldPath} <i className="fa fa-arrow-right" /> {file.newPath}
        </>
      );
    } else if (file.type === "delete") {
      return file.oldPath;
    }
    return file.newPath;
  };

  renderChangeTag = (file: any) => {
    const { t } = this.props;
    const key = "diff.changes." + file.type;
    let value = t(key);
    if (key === value) {
      value = file.type;
    }
    return <span className="tag is-info has-text-weight-normal">{value}</span>;
  };

  render() {
    const {
      file,
      fileControlFactory,
      fileAnnotationFactory,
      sideBySide,
      classes
    } = this.props;
    const { collapsed } = this.state;
    const viewType = sideBySide ? "split" : "unified";

    let body = null;
    let icon = "fa fa-angle-right";
    if (!collapsed) {
      const fileAnnotations = fileAnnotationFactory
        ? fileAnnotationFactory(file)
        : null;
      icon = "fa fa-angle-down";
      body = (
        <div className="panel-block is-paddingless is-size-7">
          {fileAnnotations}
          <DiffComponent viewType={viewType}>
            {file.hunks.map(this.renderHunk)}
          </DiffComponent>
        </div>
      );
    }

    const fileControls = fileControlFactory ? fileControlFactory(file, this.setCollapse) : null;
    return (
      <div className={classNames("panel", classes.panel)}>
        <div className="panel-heading">
          <div className="level">
            <div
              className={classNames("level-left", classes.titleHeader)}
              onClick={this.toggleCollapse}
            >
              <i className={icon} />
              <span className={classes.title}>
                {this.renderFileTitle(file)}
              </span>
              <span className={classes.changeType}>
                {this.renderChangeTag(file)}
              </span>
            </div>
            <div className="level-right">{fileControls}</div>
          </div>
        </div>
        {body}
      </div>
    );
  }
}

export default injectSheets(styles)(translate("repos")(DiffFile));

//@flow
import React from "react";
import { Link } from "react-router-dom";
import injectSheet from "react-jss";
import { ExtensionPoint, binder } from "@scm-manager/ui-extensions";
import {ButtonGroup} from "./buttons";
import classNames from "classnames";

type Props = {
  revision: string,
  path: string,
  baseUrl: string,
  classes: any
};

const styles = {
  noMargin: {
    margin: "0"
  },
  flexRow: {
    display: "flex",
    flexDirection: "row"
  },
  flexStart: {
    flex: "1"
  },
  buttonGroup: {
    alignSelf: "center",
    paddingRight: "1rem"
  }
};

class Breadcrumb extends React.Component<Props> {
  renderPath() {
    const { revision, path, baseUrl } = this.props;

    if (path) {
      const paths = path.split("/");
      const map = paths.map((path, index) => {
        const currPath = paths.slice(0, index + 1).join("/");
        if (paths.length - 1 === index) {
          return (
            <li className="is-active" key={index}>
              <Link to={"#"} aria-current="page">
                {path}
              </Link>
            </li>
          );
        }
        return (
          <li key={index}>
            <Link to={baseUrl + "/" + revision + "/" + currPath}>{path}</Link>
          </li>
        );
      });
      return map;
    }
    return <li />;
  }

  render() {
    const { classes, baseUrl, revision } = this.props;

    return (
      <>
        <div className={classes.flexRow}>
          <nav className={classNames(classes.flexStart, "breadcrumb sources-breadcrumb")} aria-label="breadcrumbs">
            <ul>{this.renderPath()}</ul>
          </nav>
          {
            binder.hasExtension("sourceView.actionbar.right") &&
            <div className={classes.buttonGroup}>
              <ButtonGroup>
                <ExtensionPoint
                  name="sourceView.actionbar.right"
                  props={{baseUrl, revision}}
                  renderAll={true}
                />
              </ButtonGroup>
            </div>
          }
        </div>
        <hr className={classes.noMargin} />
      </>
    );
  }
}

export default injectSheet(styles)(Breadcrumb);

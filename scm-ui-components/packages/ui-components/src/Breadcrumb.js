//@flow
import React from "react";
import { Link } from "react-router-dom";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import classNames from "classnames";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Branch, Repository } from "@scm-manager/ui-types";
import Icon from "./Icon";

type Props = {
  repository: Repository,
  branch: Branch,
  defaultBranch: Branch,
  branches: Branch[],
  revision: string,
  path: string,
  baseUrl: string,

  // Context props
  classes: any,
  t: string => string
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
  homeIcon: {
    lineHeight: "1.5rem"
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
              <Link to="#" aria-current="page">
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
    return null;
  }

  render() {
    const {
      baseUrl,
      branch,
      defaultBranch,
      branches,
      revision,
      path,
      repository,
      classes,
      t
    } = this.props;

    return (
      <>
        <div className={classes.flexRow}>
          <nav
            className={classNames(
              classes.flexStart,
              "breadcrumb sources-breadcrumb"
            )}
            aria-label="breadcrumbs"
          >
            <ul>
              <li>
                <Link to={baseUrl + "/" + revision + "/"}>
                  <Icon
                    className={classes.homeIcon}
                    title={t("breadcrumb.home")}
                    name="home"
                    color="inherit"
                  />
                </Link>
              </li>
              {this.renderPath()}
            </ul>
          </nav>
          {binder.hasExtension("repos.sources.actionbar") && (
            <div className={classes.buttonGroup}>
              <ExtensionPoint
                name="repos.sources.actionbar"
                props={{
                  baseUrl,
                  branch: branch ? branch : defaultBranch,
                  path,
                  isBranchUrl:
                    branches &&
                    branches.filter(
                      b => b.name.replace("/", "%2F") === revision
                    ).length > 0,
                  repository
                }}
                renderAll={true}
              />
            </div>
          )}
        </div>
        <hr className={classes.noMargin} />
      </>
    );
  }
}

export default translate("commons")(injectSheet(styles)(Breadcrumb));

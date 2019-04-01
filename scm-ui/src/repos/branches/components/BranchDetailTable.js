//@flow
import React from "react";
import type { Repository, Branch } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import classNames from "classnames";
import BranchButtonGroup from "./BranchButtonGroup";

type Props = {
  repository: Repository,
  branch: Branch,
  // context props
  t: string => string,
  classes: any
};

const styles = {
  tag: {
    marginLeft: "0.75rem",
    verticalAlign: "inherit"
  }
};

class BranchDetailTable extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    return (
      <table className="table">
        <tbody>
          <tr>
            <th>{t("branch.name")}</th>
            <td>
              {branch.name} {this.renderDefaultBranch()}
            </td>
          </tr>
          <tr>
            <th>{t("branch.repository")}</th>
            <td>{repository.name}</td>
          </tr>
          <tr>
            <th>{t("branch.actions")}</th>
            <td>
              <BranchButtonGroup repository={repository} branch={branch} />
            </td>
          </tr>
        </tbody>
      </table>
    );
  }

  renderDefaultBranch() {
    const { branch, classes } = this.props;

    let defaultLabel = null;
    if (branch.defaultBranch) {
      defaultLabel = (
        <span className={classNames("tag is-dark", classes.tag)}>Default</span>
      );
    }
    return defaultLabel;
  }
}

export default injectSheet(styles)(translate("repos")(BranchDetailTable));

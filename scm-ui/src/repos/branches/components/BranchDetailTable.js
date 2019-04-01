//@flow
import React from "react";
import type { Repository, Branch } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import BranchButtonGroup from "./BranchButtonGroup";

type Props = {
  repository: Repository,
  branch: Branch,
  // context props
  t: string => string
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
            <th>
              {t("branch.repository")}
            </th>
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
    const { branch } = this.props;

    let defaultLabel = null;
    if (branch.defaultBranch) {
      defaultLabel = <span className="tag is-dark">Default</span>;
    }
    return defaultLabel;
  }
}

export default translate("repos")(BranchDetailTable);

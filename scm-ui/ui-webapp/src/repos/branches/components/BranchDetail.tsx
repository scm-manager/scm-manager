import React from "react";
import { Branch, Repository } from "@scm-manager/ui-types";
import { WithTranslation, withTranslation } from "react-i18next";
import BranchButtonGroup from "./BranchButtonGroup";
import DefaultBranchTag from "./DefaultBranchTag";

type Props = WithTranslation & {
  repository: Repository;
  branch: Branch;
};

class BranchDetail extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    return (
      <div className="media">
        <div className="media-content subtitle">
          <strong>{t("branch.name")}</strong> {branch.name} <DefaultBranchTag defaultBranch={branch.defaultBranch} />
        </div>
        <div className="media-right">
          <BranchButtonGroup repository={repository} branch={branch} />
        </div>
      </div>
    );
  }
}

export default withTranslation("repos")(BranchDetail);

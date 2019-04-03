// @flow
import React from "react";
import BranchDetail from "./BranchDetail";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Repository, Branch } from "@scm-manager/ui-types";

type Props = {
  repository: Repository,
  branch: Branch
};

class BranchView extends React.Component<Props> {
  render() {
    const { repository, branch } = this.props;

    return (
      <div>
        <BranchDetail repository={repository} branch={branch} />
        <hr />
        <div className="content">
          <ExtensionPoint
            name="repos.branch-details.information"
            renderAll={true}
            props={{ repository, branch }}
          />
        </div>
      </div>
    );
  }
}

export default BranchView;

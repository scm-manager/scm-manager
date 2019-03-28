// @flow
import React from "react";
import BranchDetailTable from "./BranchDetailTable";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Repository, Branch } from "@scm-manager/ui-types";

type Props = {
  repository: Repository,
  branch?: Branch // TODO: get branch from props
};

class BranchView extends React.Component<Props> {
  render() {
    const { repository, branch } = this.props;
    return (
      <div>
        <BranchDetailTable repository={repository} branch={branch} />
        <hr />
        <div className="content">
          <ExtensionPoint
            name="repos.branch-details.information"
            renderAll={true}
            props={{ branch }}
          />
        </div>
      </div>
    );
  }
}

export default BranchView;

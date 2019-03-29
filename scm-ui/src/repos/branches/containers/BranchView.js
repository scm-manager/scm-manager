// @flow
import React from "react";
import BranchDetailTable from "../components/BranchDetailTable";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Repository, Branch } from "@scm-manager/ui-types";
import {connect} from "react-redux";
import {translate} from "react-i18next";
import {getBranch} from "../../modules/branches";

type Props = {
  repository: Repository,
  branch: Branch // TODO: get branch from props
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

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const branch = getBranch(state, repository, "VisualStudio"); // TODO: !!!
  return {
    repository,
    branch
  };
};

export default connect(
  mapStateToProps
)(translate("repos")(BranchView));

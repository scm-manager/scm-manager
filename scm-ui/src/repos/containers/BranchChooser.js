// @flow

import React from "react";
import type {Repository} from "@scm-manager/ui-types";
import {connect} from "react-redux";
import {fetchBranches} from "../modules/branches";
import DropDown from "../components/DropDown";

type Props = {
  repository: Repository,
  fetchBranches: Repository => void,
  callback: Branch => void, //TODO use correct branch type
  branches: Branch[], //TODO use correct branch type
  selectedBranchName: string
};

type State = {};

class BranchChooser extends React.Component<Props, State> {
  componentDidMount() {
    const { repository, fetchBranches } = this.props;
    fetchBranches(repository);
  }

  render() {
    const { selectedBranchName, branches } = this.props;
    return (
      <DropDown
        options={branches.map(b => b.name)}
        preselectedOption={selectedBranchName}
        optionSelected={branch => this.branchChanged(branch)}
      />
    );
  }

  branchChanged = (branchName: string) => {};
}

const mapStateToProps = (state: State) => {
  return {};
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchBranches: (repository: Repository) => {
      dispatch(fetchBranches(repository));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(BranchChooser);

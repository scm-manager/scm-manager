// @flow

import React from "react";
import type { Branch } from "@scm-manager/ui-types";
import DropDown from "../components/DropDown";

type Props = {
  branches: Branch[], // TODO: Use generics?
  selected?: Branch => void
};

type State = { selectedBranch?: Branch };
class BranchSelector extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }
  render() {
    const { branches } = this.props;
    if (branches) {
      return (
        <>
          <DropDown
            options={branches.map(b => b.name)}
            optionSelected={this.branchSelected}
            preselectedOption={
              this.state.selectedBranch ? this.state.selectedBranch.name : ""
            }
          />
        </>
      );
    }
  }

  branchSelected = (branchName: string) => {
    const { branches, selected } = this.props;
    const branch = branches.find(b => b.name === branchName);

    if (branch) {
      selected(branch);
      this.setState({ selectedBranch: branch });
    }
  };
}

export default BranchSelector;

// @flow

import React from "react";
import type { Repository, Branch } from "@scm-manager/ui-types";
import { connect } from "react-redux";
import {
  fetchBranches,
  getBranches,
  isFetchBranchesPending
} from "../modules/branches";

import { Loading } from "@scm-manager/ui-components";
import DropDown from "../components/DropDown";

type Props = {
  repository: Repository,
  fetchBranches: Repository => void,
  callback: (?Branch) => void,
  branches: Branch[],
  selectedBranchName: string,
  loading: boolean,
  label: string
};

type State = {
  selectedBranchName: string
};

class BranchChooser extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      selectedBranchName: props.selectedBranchName
    };
  }

  componentDidMount() {
    const { repository, fetchBranches } = this.props;
    fetchBranches(repository);
  }

  render() {
    const { branches, loading, label } = this.props;
    if (loading) {
      return <Loading />;
    }
    if (branches && branches.length > 0) {
      return (
        <div className={"box"}>
          <label className="label">{label}</label>
          <DropDown
            options={branches.map(b => b.name)}
            preselectedOption={this.state.selectedBranchName}
            optionSelected={branch => this.branchChanged(branch)}
          />
        </div>
      );
    }

    return null;
  }

  branchChanged = (branchName: string) => {
    const { callback } = this.props;
    this.setState({ ...this.state, selectedBranchName: branchName });
    const branch = this.props.branches.find(b => b.name === branchName);
    callback(branch);
  };
}

const mapStateToProps = (state: State, ownProps: Props) => {
  return {
    branches: getBranches(state, ownProps.repository),
    loading: isFetchBranchesPending(state, ownProps.repository)
  };
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

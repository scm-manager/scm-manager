// @flow
import React from "react";
import type {Branch, Repository} from "@scm-manager/ui-types";
import {connect} from "react-redux";
import {fetchBranches, getBranch, getBranchNames, isFetchBranchesPending} from "../modules/branches";
import DropDown from "../components/DropDown";
import type {History} from "history";
import {withRouter} from "react-router-dom";
import ChangesetContainer from "./ChangesetContainer";
import {Loading} from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  branches: Branch[],
  branchNames: string[],
  fetchBranches: Repository => void,
  history: History,
  match: any,
  selectedBranch: Branch,
  label: string, //TODO: Should this be here?
  loading: boolean
};
type State = {};

class BranchChangesets extends React.Component<Props, State> {
  componentDidMount() {
    this.props.fetchBranches(this.props.repository);
  }

  render() {
    const {
      repository,
      branchNames,
      match,
      selectedBranch,
      label,
      loading
    } = this.props;
    const selectedBranchName = match.params.branch;

    if (loading) {
      return <Loading />;
    }

    // TODO: Handle errors

    if (!branchNames || branchNames.length === 0) {
      return null;
    }

    return (
      <>
        <div className={"box"}>
          <label className="label">{label}</label>
          <DropDown
            options={branchNames}
            preselectedOption={selectedBranchName}
            optionSelected={branch => this.branchSelected(branch)}
          />
        </div>
        <ChangesetContainer repository={repository} branch={selectedBranch} />
      </>
    );
  }

  //TODO: Maybe extract this and let it be passed from parent component
  branchSelected = (branchName: string) => {
    const { namespace, name } = this.props.repository;
    if (branchName === "") {
      this.props.history.push(`/repo/${namespace}/${name}/changesets`);
    } else {
      this.props.history.push(
        `/repo/${namespace}/${name}/branches/${branchName}/changesets`
      );
    }
  };
}

const mapDispatchToProps = dispatch => {
  return {
    fetchBranches: (repo: Repository) => {
      dispatch(fetchBranches(repo));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const loading = isFetchBranchesPending(state, ownProps.repository);
  const branchNames = getBranchNames(state, ownProps.repository);
  const selectedBranch = getBranch(
    state,
    ownProps.repository,
    ownProps.match.params.branch //TODO: Maybe let parent component pass selected branch
  );
  return {
    loading,
    branchNames,
    selectedBranch
  };
};
export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(BranchChangesets)
);

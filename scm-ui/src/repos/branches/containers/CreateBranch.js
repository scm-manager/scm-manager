//@flow
import React from "react";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import BranchForm from "../components/BranchForm";
import type { Repository, Branch } from "@scm-manager/ui-types";
import {
  fetchBranches,
  getBranches,
  createBranch,
  createBranchReset,
  isCreateBranchPending,
  getCreateBranchFailure, isFetchBranchesPending, getFetchBranchesFailure
} from "../modules/branches";
import type { History } from "history";
import { connect } from "react-redux";

type Props = {
  loading?: boolean,
  error?: Error,
  repository: Repository,
  branches: Branch[],

  // dispatcher functions
  fetchBranches: Repository => void,
  createBranch: (branch: Branch, callback?: () => void) => void,
  resetForm: () => void,

  // context objects
  t: string => string,
  history: History
};

class CreateBranch extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranches, repository } = this.props;
    fetchBranches(repository);
    this.props.resetForm();
  }

  branchCreated = (branch: Branch) => {
    const { history } = this.props;
    history.push("/branch/" + encodeURIComponent(branch.name) + "/info");
  };

  createBranch = (branch: Branch) => {
    this.props.createBranch(branch, () => this.branchCreated(branch));
  };

  render() {
    const { t, loading, error, repository, branches } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if(!branches) {
      return <Loading/>;
    }

    return (
      <>
        <Subtitle subtitle={t("branches.create.title")} />
        <BranchForm
          submitForm={branch => this.createBranch(branch)}
          loading={loading}
          repository={repository}
          branches={branches}
        />
      </>
    );
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchBranches: (repository: Repository) => {
      dispatch(fetchBranches(repository));
    },
    createBranch: (
      repository: Repository,
      branch: Branch,
      callback?: () => void
    ) => {
      dispatch(createBranch("ghjgkj", repository, branch, callback)); //TODO
    },
    resetForm: () => {
      dispatch(createBranchReset());
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const loading = isFetchBranchesPending(state, repository) || isCreateBranchPending(state);
  const error = getFetchBranchesFailure(state, repository) || getCreateBranchFailure(state);
  const branches = getBranches(state, repository);
  return {
    repository,
    loading,
    error,
    branches
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(CreateBranch));

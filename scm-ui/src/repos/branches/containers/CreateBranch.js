//@flow
import React from "react";
import {
  ErrorNotification,
  Loading,
  Subtitle
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import BranchForm from "../components/BranchForm";
import type { Repository, Branch, BranchRequest } from "@scm-manager/ui-types";
import {
  fetchBranches,
  getBranches,
  getBranchCreateLink,
  createBranch,
  createBranchReset,
  isCreateBranchPending,
  getCreateBranchFailure,
  isFetchBranchesPending,
  getFetchBranchesFailure
} from "../modules/branches";
import type { History } from "history";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import queryString from "query-string";

type Props = {
  loading?: boolean,
  error?: Error,
  repository: Repository,
  branches: Branch[],
  createBranchesLink: string,
  isPermittedToCreateBranches: boolean,

  // dispatcher functions
  fetchBranches: Repository => void,
  createBranch: (
    createLink: string,
    repository: Repository,
    branch: BranchRequest,
    callback?: (Branch) => void
  ) => void,
  resetForm: Repository => void,

  // context objects
  t: string => string,
  history: History,
  location: any
};

class CreateBranch extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranches, repository } = this.props;
    fetchBranches(repository);
    this.props.resetForm(repository);
  }

  branchCreated = (branch: Branch) => {
    const { history, repository } = this.props;
    history.push(
      `/repo/${repository.namespace}/${
        repository.name
      }/branch/${encodeURIComponent(branch.name)}/info`
    );
  };

  createBranch = (branch: BranchRequest) => {
    this.props.createBranch(
      this.props.createBranchesLink,
      this.props.repository,
      branch,
      newBranch => this.branchCreated(newBranch)
    );
  };

  transmittedName = (url: string) => {
    const params = queryString.parse(url);
    return params.name;
  };

  render() {
    const { t, loading, error, repository, branches, createBranchesLink, location } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading || !branches) {
      return <Loading />;
    }

    return (
      <>
        <Subtitle subtitle={t("branches.create.title")} />
        <BranchForm
          submitForm={branchRequest => this.createBranch(branchRequest)}
          loading={loading}
          repository={repository}
          branches={branches}
          transmittedName={this.transmittedName(location.search)}
          disabled={!createBranchesLink}
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
      createLink: string,
      repository: Repository,
      branchRequest: BranchRequest,
      callback?: (newBranch: Branch) => void
    ) => {
      dispatch(createBranch(createLink, repository, branchRequest, callback));
    },
    resetForm: (repository: Repository) => {
      dispatch(createBranchReset(repository));
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const loading =
    isFetchBranchesPending(state, repository) ||
    isCreateBranchPending(state, repository);
  const error =
    getFetchBranchesFailure(state, repository) || getCreateBranchFailure(state);
  const branches = getBranches(state, repository);
  const createBranchesLink = getBranchCreateLink(state, repository);
  return {
    repository,
    loading,
    error,
    branches,
    createBranchesLink
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("repos")(CreateBranch))
);

import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import queryString from "query-string";
import { History } from "history";
import { Branch, BranchRequest, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import BranchForm from "../components/BranchForm";
import {
  createBranch,
  createBranchReset,
  fetchBranches,
  getBranchCreateLink,
  getBranches,
  getCreateBranchFailure,
  getFetchBranchesFailure,
  isCreateBranchPending,
  isFetchBranchesPending
} from "../modules/branches";
import { compose } from "redux";

type Props = WithTranslation & {
  loading?: boolean;
  error?: Error;
  repository: Repository;
  branches: Branch[];
  createBranchesLink: string;
  isPermittedToCreateBranches: boolean;

  // dispatcher functions
  fetchBranches: (p: Repository) => void;
  createBranch: (
    createLink: string,
    repository: Repository,
    branch: BranchRequest,
    callback?: (p: Branch) => void
  ) => void;
  resetForm: (p: Repository) => void;

  // context objects
  history: History;
  location: any;
};

class CreateBranch extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranches, repository } = this.props;
    fetchBranches(repository);
    this.props.resetForm(repository);
  }

  branchCreated = (branch: Branch) => {
    const { history, repository } = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}/branch/${encodeURIComponent(branch.name)}/info`);
  };

  createBranch = (branch: BranchRequest) => {
    this.props.createBranch(this.props.createBranchesLink, this.props.repository, branch, newBranch =>
      this.branchCreated(newBranch)
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

const mapDispatchToProps = (dispatch: any) => {
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

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository } = ownProps;
  const loading = isFetchBranchesPending(state, repository) || isCreateBranchPending(state, repository);
  const error = getFetchBranchesFailure(state, repository) || getCreateBranchFailure(state, repository);
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

export default compose(
  withTranslation("repos"),
  connect(mapStateToProps, mapDispatchToProps),
  withRouter
)(CreateBranch);

// @flow
import React from "react";
import {fetchBranches, getBranches, getFetchBranchesFailure, isFetchBranchesPending} from "../modules/branches";
import {connect} from "react-redux";
import type {Branch, Repository} from "@scm-manager/ui-types";
import {compose} from "redux";
import {translate} from "react-i18next";
import {withRouter} from "react-router-dom";
import {ErrorNotification, Loading} from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  loading: boolean,
  error: Error,
  branches: Branch[],

  // dispatch props
  fetchBranches: Repository => void,

  // Context props
  history: any,
  match: any,
  t: string => string
};
class BranchesOverview extends React.Component<Props> {
  componentDidMount() {
    const {
      fetchBranches,
      repository
    } = this.props;

    fetchBranches(repository);
  }

  render() {
    const {
      loading,
      error,
    } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }

    return <>{this.renderBranches()}</>;
  }

  renderBranches() {
    const { branches } = this.props;

    let branchesList = null;
    if (branches) {
      branchesList = (
        <ul>
          {branches.map((branch, index) => {
            return <li key={index}>{branch.name}</li>;
          })}
        </ul>
      );
    }
    return branchesList;
  }
}

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const loading = isFetchBranchesPending(state, repository);
  const error = getFetchBranchesFailure(state, repository);
  const branches = getBranches(state, repository);

  return {
    repository,
    loading,
    error,
    branches
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchBranches: (repository: Repository) => {
      dispatch(fetchBranches(repository));
    },
  };
};

export default compose(
  translate("repos"),
  withRouter,
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(BranchesOverview);

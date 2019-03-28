// @flow
import React from "react";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../../modules/branches";
import { connect } from "react-redux";
import type { Branch, Repository } from "@scm-manager/ui-types";
import { compose } from "redux";
import { translate } from "react-i18next";
import { withRouter } from "react-router-dom";
import {
  CreateButton,
  ErrorNotification,
  Loading,
  Subtitle
} from "@scm-manager/ui-components";
import BranchTable from "../components/BranchTable";

type Props = {
  repository: Repository,
  baseUrl: string,
  loading: boolean,
  error: Error,
  branches: Branch[],

  // dispatch props
  showCreateButton: boolean,
  fetchBranches: Repository => void,

  // Context props
  history: any,
  match: any,
  t: string => string
};
class BranchesOverview extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranches, repository } = this.props;

    fetchBranches(repository);
  }

  render() {
    const { baseUrl, loading, error, branches, t } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }

    return (
      <>
        <Subtitle subtitle={t("branches.overview.title")} />
        <BranchTable baseUrl={baseUrl} branches={branches} />
        {this.renderCreateButton()}
      </>
    );
  }

  renderCreateButton() {
    const { showCreateButton, t } = this.props;
    if (showCreateButton || true ) { // TODO
      return (
        <CreateButton label={t("branches.overview.createButton")} link="./create" />
      );
    }
    return null;
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
    }
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

// @flow
import React from "react";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../modules/branches";
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

// master, default should always be the first one,
// followed by develop the rest should be ordered by its name
export function orderBranches(branches: Branch[]) {
  branches.sort((a, b) => {
    if (a.defaultBranch && !b.defaultBranch) {
      return -20;
    } else if (!a.defaultBranch && b.defaultBranch) {
      return 20;
    } else if (a.name === "master" && b.name !== "master") {
      return -10;
    } else if (a.name !== "master" && b.name === "master") {
      return 10;
    } else if (a.name === "default" && b.name !== "default") {
      return -10;
    } else if (a.name !== "default" && b.name === "default") {
      return 10;
    } else if (a.name === "develop" && b.name !== "develop") {
      return -5;
    } else if (a.name !== "develop" && b.name === "develop") {
      return 5;
    } else if (a.name < b.name) {
      return -1;
    } else if (a.name > b.name) {
      return 1;
    }
    return 0;
  });
}

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

    if (!branches || loading) {
      return <Loading />;
    }

    orderBranches(branches);

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
    if (showCreateButton || true) {
      // TODO
      return (
        <CreateButton
          label={t("branches.overview.createButton")}
          link="./create"
        />
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

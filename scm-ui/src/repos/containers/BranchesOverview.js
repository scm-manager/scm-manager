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
import {Link, withRouter} from "react-router-dom";
import {
  ErrorNotification,
  Loading,
  Subtitle
} from "@scm-manager/ui-components";

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
    const { fetchBranches, repository } = this.props;

    fetchBranches(repository);
  }

  render() {
    const { loading, error, t } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }

    return (
      <>
        <Subtitle subtitle={t("branchesOverview.title")} />
        <table className="card-table table is-hoverable is-fullwidth">
          <thead>
            <tr>
              <th>{t("branchesOverview.branches")}</th>
            </tr>
          </thead>
          <tbody>{this.renderBranches()}</tbody>
        </table>
      </>
    );
  }

  renderBranches() {
    const { branches } = this.props;

    let branchesList = null;
    if (branches) {
      branchesList = (
        <>
          {branches.map((branch, index) => {
            const to = `../branch/${encodeURIComponent(branch.name)}/changesets/`;
            return (
              <tr>
                <td key={index}><Link to={to}>{branch.name}</Link></td>
              </tr>
            );
          })}
        </>
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

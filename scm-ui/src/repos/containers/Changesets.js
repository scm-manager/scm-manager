// @flow

import React from "react";
import { withRouter } from "react-router-dom";
import type {
  Branch,
  Changeset,
  PagedCollection,
  Repository
} from "@scm-manager/ui-types";
import {
  fetchChangesetsByBranch,
  fetchChangesetsByBranchAndPage,
  getChangesets,
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  selectListAsCollection
} from "../modules/changesets";
import { connect } from "react-redux";
import ChangesetList from "../components/changesets/ChangesetList";
import { ErrorPage, LinkPaginator, Loading } from "@scm-manager/ui-components";

type Props = {
  fetchChangesetsByBranch: (Repository, Branch) => void,
  fetchChangesetsByBranchAndPage: (Repository, Branch, number) => void,
  repository: Repository, //TODO: Do we really need/want this here?
  branch: Branch,
  changesets: Changeset[],
  loading: boolean,
  match: any,
  list: PagedCollection,
  error: Error
};

type State = {};

class ChangesetContainer extends React.Component<Props, State> {
  componentDidMount() {
    const {
      fetchChangesetsByBranch,
      fetchChangesetsByBranchAndPage,
      repository,
      branch,
      match
    } = this.props;
    const { page } = match.params;
    if (!page) {
      fetchChangesetsByBranch(repository, branch);
    } else {
      fetchChangesetsByBranchAndPage(repository, branch, page);
    }
  }

  render() {
    const { changesets, loading, error } = this.props;

    // TODO: i18n
    if (error) {
      return (
        <ErrorPage
          title={"Failed loading branches"}
          subtitle={"Somethin went wrong"}
          error={error}
        />
      );
    }

    if (loading) {
      return <Loading />;
    }
    if (!changesets || changesets.length === 0) {
      return null;
    }
    return (
      <>
        {this.renderList()}
        {this.renderPaginator()}
      </>
    );
  }

  renderList = () => {
    const { repository, changesets } = this.props;
    return <ChangesetList repository={repository} changesets={changesets} />;
  };

  renderPaginator = () => {
    const { list } = this.props;
    if (list) {
      return <LinkPaginator collection={list} />;
    }
    return null;
  };
}

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetsByBranch: (repo: Repository, branch: Branch) => {
      dispatch(fetchChangesetsByBranch(repo, branch));
    },
    fetchChangesetsByBranchAndPage: (
      repo: Repository,
      branch: Branch,
      page: number
    ) => {
      dispatch(fetchChangesetsByBranchAndPage(repo, branch, page));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, branch } = ownProps;
  const changesets = getChangesets(state, repository, branch);
  const loading = isFetchChangesetsPending(state, repository, branch);
  const error = getFetchChangesetsFailure(state, repository, branch);
  const list = selectListAsCollection(state, repository, branch);
  return { changesets, list, loading, error };
};
export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(ChangesetContainer)
);

// @flow

import React from "react";
import {withRouter} from "react-router-dom";
import type {Branch, Changeset, PagedCollection, Repository} from "@scm-manager/ui-types";
import {
  fetchChangesetsByBranch,
  fetchChangesetsByBranchAndPage,
  getChangesets,
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  selectListAsCollection
} from "../modules/changesets";
import {connect} from "react-redux";
import ChangesetList from "../components/changesets/ChangesetList";
import {ErrorPage, LinkPaginator, Loading} from "@scm-manager/ui-components";
import {translate} from "react-i18next";

type Props = {
  repository: Repository, //TODO: Do we really need/want this here?
  branch: Branch,

  // State props
  changesets: Changeset[],
  loading: boolean,
  list: PagedCollection,
  error: Error,

  // Dispatch props
  fetchChangesetsByBranch: (Repository, Branch) => void,
  fetchChangesetsByBranchAndPage: (Repository, Branch, number) => void,

  // Context Props
  match: any,
  t: string => string
};

type State = {};

class Changesets extends React.Component<Props, State> {
  componentDidMount() {
    console.log("CDM");
    const {
      fetchChangesetsByBranch,
      fetchChangesetsByBranchAndPage,
      repository,
      branch,
      match
    } = this.props;

    const { page } = match.params;
    if (!branch) {
      return;
    }
    if (!page) {
      fetchChangesetsByBranch(repository, branch);
    } else {
      fetchChangesetsByBranchAndPage(repository, branch, page);
    }
  }

  // componentDidUpdate(prevProps: Props) {
  //   const {
  //     match,
  //     repository,
  //     branch,
  //     fetchChangesetsByBranch,
  //     fetchChangesetsByBranchAndPage
  //   } = this.props;
  //   const { page } = match.params;
  //
  //   if (branch === prevProps.branch) {
  //     return;
  //   }
  //
  //   if (!page) {
  //     fetchChangesetsByBranch(repository, branch);
  //   } else {
  //     fetchChangesetsByBranchAndPage(repository, branch, page);
  //   }
  // }

  render() {
    const { changesets, loading, error, t } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("changesets.error-title")}
          subtitle={t("changesets.error-title")}
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
  )(translate("repos")(Changesets))
);

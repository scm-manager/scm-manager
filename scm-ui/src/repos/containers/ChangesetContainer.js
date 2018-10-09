// @flow

import React from "react";
import { withRouter } from "react-router-dom";
import type { Branch, Changeset, Repository } from "@scm-manager/ui-types";
import {
  fetchChangesetsByBranch,
  getChangesets,
  isFetchChangesetsPending
} from "../modules/changesets";
import { connect } from "react-redux";
import ChangesetList from "../components/changesets/ChangesetList";
import { Loading } from "@scm-manager/ui-components";

type Props = {
  fetchChangesetsByBranch: (Repository, Branch) => void,
  repository: Repository, //TODO: Do we really need/want this here?
  branch: Branch,
  changesets: Changeset[],
  loading: boolean
};
type State = {};

class ChangesetContainer extends React.Component<Props, State> {
  componentDidMount() {
    const { fetchChangesetsByBranch, repository, branch } = this.props;
    fetchChangesetsByBranch(repository, branch); //TODO: fetch by page
  }

  render() {
    const { repository, changesets, loading } = this.props;
    if (loading) {
      return <Loading />;
    }
    if (!changesets || changesets.length === 0) {
      return null;
    }
    return <ChangesetList repository={repository} changesets={changesets} />;
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetsByBranch: (repo: Repository, branch: Branch) => {
      dispatch(fetchChangesetsByBranch(repo, branch));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, branch } = ownProps;
  const changesets = getChangesets(state, repository, branch);
  const loading = isFetchChangesetsPending(state, repository, branch);
  return { changesets, loading };
};
export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(ChangesetContainer)
);

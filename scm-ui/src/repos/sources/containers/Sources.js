// @flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import type { Repository, Branch } from "@scm-manager/ui-types";
import FileTree from "../components/FileTree";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import BranchSelector from "../../containers/BranchSelector";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../../modules/branches";
import { compose } from "redux";

type Props = {
  repository: Repository,
  loading: boolean,
  error: Error,
  baseUrl: string,
  branches: Branch[],
  revision: string,
  path: string,

  // dispatch props
  fetchBranches: Repository => void,

  // Context props
  history: any,
  match: any
};

class Sources extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranches, repository } = this.props;

    fetchBranches(repository);
  }

  branchSelected = (branch?: Branch) => {
    const { baseUrl, history, path } = this.props;

    let url;
    if (branch) {
      if (path) {
        url = `${baseUrl}/${branch.name}/${path}`;
      } else {
        url = `${baseUrl}/${branch.name}/`;
      }
    } else {
      url = `${baseUrl}/`;
    }
    history.push(url);
  };

  render() {
    const { repository, baseUrl, loading, error, revision, path } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }

    return (
      <>
        {this.renderBranchSelector()}
        <FileTree
          repository={repository}
          revision={revision}
          path={path}
          baseUrl={baseUrl}
        />
      </>
    );
  }

  renderBranchSelector = () => {
    const { repository, branches } = this.props;
    if (repository._links.branches) {
      return (
        <BranchSelector
          branches={branches}
          selected={(b: Branch) => {
            this.branchSelected(b);
          }}
        />
      );
    }
    return null;
  };
}

const mapStateToProps = (state, ownProps) => {
  const { repository, match } = ownProps;
  const { revision, path } = match.params;

  const loading = isFetchBranchesPending(state, repository);
  const error = getFetchBranchesFailure(state, repository);
  const branches = getBranches(state, repository);

  return {
    repository,
    revision,
    path,
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
  withRouter,
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(Sources);

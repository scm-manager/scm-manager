// @flow

import React from "react";
import type { Repository, Branch } from "@scm-manager/ui-types";
import { Route, Switch, withRouter } from "react-router-dom";
import Changesets from "./Changesets";
import BranchSelector from "./BranchSelector";
import { connect } from "react-redux";
import { ErrorPage, Loading } from "@scm-manager/ui-components";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../modules/branches";
import { compose } from "redux";

type Props = {
  repository: Repository,
  baseUrl: string,

  // State props
  branches: Branch[],
  loading: boolean,

  // Dispatch props
  fetchBranches: Repository => void,

  // Context props
  history: History,
  match: any
};

type State = {
  selectedBranch?: Branch
};

class BranchRoot extends React.PureComponent<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    this.props.fetchBranches(this.props.repository);
  }

  componentDidUpdate(prevProps: Props) {
    const { branches, match, loading } = this.props;
    console.log("BR did update");
    const branchName = decodeURIComponent(match.params.branch);
    if (branches) {
      if (
        (!loading && prevProps.loading) ||
        match.url !== prevProps.match.url
      ) {
        this.setState({
          selectedBranch: branches.find(b => b.name === branchName)
        });
      }
    }
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.baseUrl);
  };

  branchSelected = (branch: Branch) => {
    const url = this.matchedUrl();
    this.props.history.push(
      `${url}/${encodeURIComponent(branch.name)}/changesets/`
    );
  };

  render() {
    const { repository, match, branches, loading } = this.props;
    const url = this.stripEndingSlash(match.url);

    if (loading) {
      return <Loading />;
    }

    if (!repository || !branches) {
      return null;
    }


    return (
      <>
        <BranchSelector
          branches={branches}
          selected={(b: Branch) => {
            this.branchSelected(b);
          }}
        />
        <Route
          path={`${url}/changesets/:page?`}
          component={() => (
            <Changesets
              repository={repository}
              branch={this.state.selectedBranch}
            />
          )}
        />

      </>
    );
  }
}


const mapDispatchToProps = dispatch => {
  return {
    fetchBranches: (repo: Repository) => {
      dispatch(fetchBranches(repo));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository } = ownProps;
  const loading = isFetchBranchesPending(state, repository);
  const error = getFetchBranchesFailure(state, repository);

  const branches = getBranches(state, repository);
  return {
    loading,
    error,
    branches
  };
};

export default compose(
  connect(
    mapStateToProps,
    mapDispatchToProps
  ),
  withRouter
)(BranchRoot);

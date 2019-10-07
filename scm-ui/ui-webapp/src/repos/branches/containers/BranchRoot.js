//@flow
import React from "react";
import BranchView from "../components/BranchView";
import { connect } from "react-redux";
import { Redirect, Route, Switch, withRouter } from "react-router-dom";
import type { Repository, Branch } from "@scm-manager/ui-types";
import {
  fetchBranch,
  getBranch,
  getFetchBranchFailure,
  isFetchBranchPending
} from "../modules/branches";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import type { History } from "history";
import { NotFoundError } from "@scm-manager/ui-components";
import queryString from "query-string";

type Props = {
  repository: Repository,
  branchName: string,
  branch: Branch,
  loading: boolean,
  error?: Error,

  // context props
  history: History,
  match: any,
  location: any,

  // dispatch functions
  fetchBranch: (repository: Repository, branchName: string) => void
};

class BranchRoot extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranch, repository, branchName } = this.props;

    fetchBranch(repository, branchName);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { repository, branch, loading, error, match, location } = this.props;

    const url = this.matchedUrl();

    if (error) {
      if (
        error instanceof NotFoundError &&
        queryString.parse(location.search).create === "true"
      ) {
        return (
          <Redirect
            to={`/repo/${repository.namespace}/${
              repository.name
            }/branches/create?name=${match.params.branch}`}
          />
        );
      }

      return <ErrorNotification error={error} />;
    }

    if (loading || !branch) {
      return <Loading />;
    }

    return (
      <Switch>
        <Redirect exact from={url} to={`${url}/info`} />
        <Route
          path={`${url}/info`}
          component={() => (
            <BranchView repository={repository} branch={branch} />
          )}
        />
      </Switch>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const branchName = decodeURIComponent(ownProps.match.params.branch);
  const branch = getBranch(state, repository, branchName);
  const loading = isFetchBranchPending(state, repository, branchName);
  const error = getFetchBranchFailure(state, repository, branchName);
  return {
    repository,
    branchName,
    branch,
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchBranch: (repository: Repository, branchName: string) => {
      dispatch(fetchBranch(repository, branchName));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(BranchRoot)
);

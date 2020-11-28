/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import BranchView from "../components/BranchView";
import { connect } from "react-redux";
import { compose } from "redux";
import { Redirect, Route, Switch, withRouter } from "react-router-dom";
import { Branch, Repository } from "@scm-manager/ui-types";
import { fetchBranch, getBranch, getFetchBranchFailure, isFetchBranchPending } from "../modules/branches";
import { ErrorNotification, Loading, NotFoundError, urls } from "@scm-manager/ui-components";
import { History } from "history";
import queryString from "query-string";

type Props = {
  repository: Repository;
  branchName: string;
  branch: Branch;
  loading: boolean;
  error?: Error;

  // context props
  history: History;
  match: any;
  location: any;

  // dispatch functions
  fetchBranch: (repository: Repository, branchName: string) => void;
};

class BranchRoot extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranch, repository, branchName } = this.props;
    fetchBranch(repository, branchName);
  }

  render() {
    const { repository, branch, loading, error, match, location } = this.props;

    const url = urls.matchedUrl(this.props);

    if (error) {
      if (error instanceof NotFoundError && queryString.parse(location.search).create === "true") {
        return (
          <Redirect
            to={`/repo/${repository.namespace}/${repository.name}/branches/create?name=${match.params.branch}`}
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
        <Route path={`${url}/info`} component={() => <BranchView repository={repository} branch={branch} />} />
      </Switch>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
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

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchBranch: (repository: Repository, branchName: string) => {
      dispatch(fetchBranch(repository, branchName));
    }
  };
};

export default compose(withRouter, connect(mapStateToProps, mapDispatchToProps))(BranchRoot);

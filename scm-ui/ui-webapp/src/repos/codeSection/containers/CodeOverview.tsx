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
import { Route, RouteComponentProps, withRouter } from "react-router-dom";
import Sources from "../../sources/containers/Sources";
import ChangesetsRoot from "../../containers/ChangesetsRoot";
import { Branch, Repository } from "@scm-manager/ui-types";
import { ErrorPage, Loading } from "@scm-manager/ui-components";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { connect } from "react-redux";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../../branches/modules/branches";

type Props = RouteComponentProps &
  WithTranslation & {
    repository: Repository;
    baseUrl: string;

    // State props
    branches: Branch[];
    error: Error;
    loading: boolean;
    selectedBranch: string;

    // Dispatch props
    fetchBranches: (p: Repository) => void;
  };

class CodeOverview extends React.Component<Props> {
  componentDidMount() {
    const { repository } = this.props;
    this.props.fetchBranches(repository);
  }

  render() {
    const { repository, baseUrl, branches, selectedBranch, error, loading, t } = this.props;
    const url = baseUrl;

    if (loading) {
      return <Loading />;
    }

    if (error) {
      return (
        <ErrorPage title={t("repositoryRoot.errorTitle")} subtitle={t("repositoryRoot.errorSubtitle")} error={error} />
      );
    }

    return (
      <>
        <Route
          path={`${url}/sources`}
          exact={true}
          render={() => <Sources repository={repository} baseUrl={`${url}`} branches={branches} />}
        />
        <Route
          path={`${url}/sources/:revision/:path*`}
          render={() => (
            <Sources repository={repository} baseUrl={`${url}`} branches={branches} selectedBranch={selectedBranch} />
          )}
        />
        <Route
          path={`${url}/changesets`}
          render={() => <ChangesetsRoot repository={repository} baseUrl={`${url}`} branches={branches} />}
        />
        <Route
          path={`${url}/branch/:branch/changesets/`}
          render={() => (
            <ChangesetsRoot
              repository={repository}
              baseUrl={`${url}`}
              branches={branches}
              selectedBranch={selectedBranch}
            />
          )}
        />
      </>
    );
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchBranches: (repo: Repository) => {
      dispatch(fetchBranches(repo));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, location } = ownProps;
  const error = getFetchBranchesFailure(state, repository);
  const loading = isFetchBranchesPending(state, repository);
  const branches = getBranches(state, repository);
  const branchFromURL =
    !location.pathname.includes("/code/changesets/") && decodeURIComponent(location.pathname.split("/")[6]);
  const selectedBranch = branchFromURL && branchFromURL !== "undefined" ? branchFromURL : "";
  return {
    error,
    loading,
    branches,
    selectedBranch
  };
};

export default compose(
  withRouter,
  withTranslation("repos"),
  connect(mapStateToProps, mapDispatchToProps)
)(CodeOverview);

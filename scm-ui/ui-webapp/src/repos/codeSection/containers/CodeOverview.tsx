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

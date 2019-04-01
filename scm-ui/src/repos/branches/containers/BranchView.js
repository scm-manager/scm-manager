// @flow
import React from "react";
import BranchDetailTable from "../components/BranchDetailTable";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Repository, Branch } from "@scm-manager/ui-types";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { withRouter } from "react-router-dom";
import {
  fetchBranchByName,
  getBranchByName,
  getFetchBranchFailure,
  isFetchBranchPending
} from "../../modules/branches";
import { ErrorPage, Loading } from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  branchName: string,
  loading: boolean,
  error: Error,
  branch: Branch,

  // dispatch functions
  fetchBranchByName: (repository: Repository, branchName: string) => void,

  // context props
  t: string => string
};

class BranchView extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranchByName, repository, branchName } = this.props;

    fetchBranchByName(repository, branchName);
  }

  render() {
    const { loading, error, t, repository, branch } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("branches.errorTitle")}
          subtitle={t("branches.errorSubtitle")}
          error={error}
        />
      );
    }

    if (!branch || loading) {
      return <Loading />;
    }

    return (
      <div>
        <BranchDetailTable repository={repository} branch={branch} />
        <hr />
        <div className="content">
          <ExtensionPoint
            name="repos.branch-details.information"
            renderAll={true}
            props={{ repository, branch }}
          />
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const branchName = decodeURIComponent(ownProps.match.params.branch);
  const branch = getBranchByName(state, branchName);
  const loading = isFetchBranchPending(state, branchName);
  const error = getFetchBranchFailure(state, branchName);
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
    fetchBranchByName: (repository: Repository, branchName: string) => {
      dispatch(fetchBranchByName(repository._links.branches.href, branchName));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("repos")(BranchView))
);

import React from "react";
import { connect } from "react-redux";
import { compose } from "redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, Repository } from "@scm-manager/ui-types";
import { CreateButton, ErrorNotification, Loading, Notification, Subtitle } from "@scm-manager/ui-components";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending,
  isPermittedToCreateBranches
} from "../modules/branches";
import { orderBranches } from "../util/orderBranches";
import BranchTable from "../components/BranchTable";

type Props = WithTranslation & {
  repository: Repository;
  baseUrl: string;
  loading: boolean;
  error: Error;
  branches: Branch[];

  // dispatch props
  showCreateButton: boolean;
  fetchBranches: (p: Repository) => void;

  // Context props
  history: any;
  match: any;
};

class BranchesOverview extends React.Component<Props> {
  componentDidMount() {
    const { fetchBranches, repository } = this.props;
    fetchBranches(repository);
  }

  render() {
    const { loading, error, branches, t } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (!branches || loading) {
      return <Loading />;
    }

    return (
      <>
        <Subtitle subtitle={t("branches.overview.title")} />
        {this.renderBranchesTable()}
        {this.renderCreateButton()}
      </>
    );
  }

  renderBranchesTable() {
    const { baseUrl, branches, t } = this.props;
    if (branches && branches.length > 0) {
      orderBranches(branches);
      return <BranchTable baseUrl={baseUrl} branches={branches} />;
    }
    return <Notification type="info">{t("branches.overview.noBranches")}</Notification>;
  }

  renderCreateButton() {
    const { showCreateButton, t } = this.props;
    if (showCreateButton) {
      return <CreateButton label={t("branches.overview.createButton")} link="./create" />;
    }
    return null;
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository } = ownProps;
  const loading = isFetchBranchesPending(state, repository);
  const error = getFetchBranchesFailure(state, repository);
  const branches = getBranches(state, repository);
  const showCreateButton = isPermittedToCreateBranches(state, repository);

  return {
    repository,
    loading,
    error,
    branches,
    showCreateButton
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchBranches: (repository: Repository) => {
      dispatch(fetchBranches(repository));
    }
  };
};

export default compose(
  withTranslation("repos"),
  withRouter,
  connect(mapStateToProps, mapDispatchToProps)
)(BranchesOverview);

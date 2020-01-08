import React from "react";
import { connect } from "react-redux";
import { compose } from "redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { Repository } from "@scm-manager/ui-types";
import { Level, DeleteButton, confirmAlert, ErrorNotification } from "@scm-manager/ui-components";
import { deleteRepo, getDeleteRepoFailure, isDeleteRepoPending } from "../modules/repos";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  repository: Repository;
  confirmDialog?: boolean;
  deleteRepo: (p1: Repository, p2: () => void) => void;

  // context props
  history: History;
};

class DeleteRepo extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleted = () => {
    this.props.history.push("/repos/");
  };

  deleteRepo = () => {
    this.props.deleteRepo(this.props.repository, this.deleted);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("deleteRepo.confirmAlert.title"),
      message: t("deleteRepo.confirmAlert.message"),
      buttons: [
        {
          className: "is-outlined",
          label: t("deleteRepo.confirmAlert.submit"),
          onClick: () => this.deleteRepo()
        },
        {
          label: t("deleteRepo.confirmAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.repository._links.delete;
  };

  render() {
    const { loading, error, confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteRepo;

    if (!this.isDeletable()) {
      return null;
    }

    return (
      <>
        <hr />
        <ErrorNotification error={error} />
        <Level right={<DeleteButton label={t("deleteRepo.button")} action={action} loading={loading} />} />
      </>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const loading = isDeleteRepoPending(state, namespace, name);
  const error = getDeleteRepoFailure(state, namespace, name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    deleteRepo: (repo: Repository, callback: () => void) => {
      dispatch(deleteRepo(repo, callback));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withRouter, withTranslation("repos"))(DeleteRepo);

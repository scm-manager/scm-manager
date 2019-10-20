import React from "react";
import { translate } from "react-i18next";
import { RepositoryRole } from "@scm-manager/ui-types";
import {
  Subtitle,
  DeleteButton,
  confirmAlert,
  ErrorNotification
} from "@scm-manager/ui-components";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { History } from "history";
import {
  deleteRole,
  getDeleteRoleFailure,
  isDeleteRolePending
} from "../modules/roles";

type Props = {
  loading: boolean;
  error: Error;
  role: RepositoryRole;
  confirmDialog?: boolean;
  deleteRole: (role: RepositoryRole, callback?: () => void) => void;

  // context props
  history: History;
  t: (p: string) => string;
};

class DeleteRepositoryRole extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  roleDeleted = () => {
    this.props.history.push("/admin/roles/");
  };

  deleteRole = () => {
    this.props.deleteRole(this.props.role, this.roleDeleted);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("repositoryRole.delete.confirmAlert.title"),
      message: t("repositoryRole.delete.confirmAlert.message"),
      buttons: [
        {
          label: t("repositoryRole.delete.confirmAlert.submit"),
          onClick: () => this.deleteRole()
        },
        {
          label: t("repositoryRole.delete.confirmAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.role._links.delete;
  };

  render() {
    const { loading, error, confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteRole;

    if (!this.isDeletable()) {
      return null;
    }

    return (
      <>
        <Subtitle subtitle={t("repositoryRole.delete.subtitle")} />
        <div className="columns">
          <div className="column">
            <ErrorNotification error={error} />
            <DeleteButton
              label={t("repositoryRole.delete.button")}
              action={action}
              loading={loading}
            />
          </div>
        </div>
      </>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isDeleteRolePending(state, ownProps.role.name);
  const error = getDeleteRoleFailure(state, ownProps.role.name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    deleteRole: (role: RepositoryRole, callback?: () => void) => {
      dispatch(deleteRole(role, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(translate("admin")(DeleteRepositoryRole)));

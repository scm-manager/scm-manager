import React from "react";
import RepositoryRoleForm from "./RepositoryRoleForm";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { getModifyRoleFailure, isModifyRolePending, modifyRole } from "../modules/roles";
import { ErrorNotification, Subtitle, Loading } from "@scm-manager/ui-components";
import { RepositoryRole } from "@scm-manager/ui-types";
import { History } from "history";
import DeleteRepositoryRole from "./DeleteRepositoryRole";
import { compose } from "redux";

type Props = WithTranslation & {
  disabled: boolean;
  role: RepositoryRole;
  repositoryRolesLink: string;
  loading?: boolean;
  error?: Error;

  // context objects
  history: History;

  //dispatch function
  updateRole: (role: RepositoryRole, callback?: () => void) => void;
};

class EditRepositoryRole extends React.Component<Props> {
  repositoryRoleUpdated = () => {
    this.props.history.push("/admin/roles/");
  };

  updateRepositoryRole = (role: RepositoryRole) => {
    this.props.updateRole(role, this.repositoryRoleUpdated);
  };

  render() {
    const { loading, error, t } = this.props;

    if (loading) {
      return <Loading />;
    } else if (error) {
      return <ErrorNotification error={error} />;
    }

    return (
      <>
        <Subtitle subtitle={t("repositoryRole.editSubtitle")} />
        <RepositoryRoleForm
          role={this.props.role}
          submitForm={(role: RepositoryRole) => this.updateRepositoryRole(role)}
        />
        <DeleteRepositoryRole role={this.props.role} />
      </>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const loading = isModifyRolePending(state, ownProps.role.name);
  const error = getModifyRoleFailure(state, ownProps.role.name);

  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    updateRole: (role: RepositoryRole, callback?: () => void) => {
      dispatch(modifyRole(role, callback));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withTranslation("admin"))(EditRepositoryRole);

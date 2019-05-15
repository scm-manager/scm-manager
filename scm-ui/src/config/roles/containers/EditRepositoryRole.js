// @flow
import React from "react";
import RepositoryRoleForm from "./RepositoryRoleForm";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import {
  getModifyRoleFailure,
  isModifyRolePending,
  modifyRole,
} from "../modules/roles";
import type { Role } from "@scm-manager/ui-types";

type Props = {
  disabled: boolean,
  role: Role,
  repositoryRolesLink: string,

  //dispatch function
  updateRole: (link: string, role: Role, callback?: () => void) => void
};



class EditRepositoryRole extends React.Component<Props> {

  repositoryRoleUpdated = (role: Role) => {
    const { history } = this.props;
    history.push("/config/role/" + role.name);
  };

  updateRepositoryRole = (role: Role) => {
    this.props.updateRole(role, () =>
      this.repositoryRoleUpdated(role)
    );
  };

  render() {
    return (
      <>
        <RepositoryRoleForm
          disabled={false}
          role={this.props.role}
          submitForm={role => this.updateRepositoryRole(role)}
        />
      </>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isModifyRolePending(state);
  const error = getModifyRoleFailure(state);

  return {
    loading,
    error,
  };
};

const mapDispatchToProps = dispatch => {
  return {
    updateRole: (role: Role, callback?: () => void) => {
      dispatch(modifyRole(role, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("config")(EditRepositoryRole));

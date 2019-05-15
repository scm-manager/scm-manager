// @flow
import React from "react";
import RepositoryRoleForm from "./RepositoryRoleForm";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import {
  createRole,
  getFetchVerbsFailure,
  isFetchVerbsPending
} from "../modules/roles";
import type { Role } from "@scm-manager/ui-types";
import {
  getRepositoryRolesLink,
  getRepositoryVerbsLink
} from "../../../modules/indexResource";

type Props = {
  disabled: boolean,
  repositoryRolesLink: string,

  //dispatch function
  addRole: (link: string, role: Role) => void
};

class CreateRepositoryRole extends React.Component<Props> {
  //Callback after dispatch
  repositoryRoleCreated = (role: Role) => {
    const { history } = this.props;
    history.push("/role/" + role.name);
  };

  createRepositoryRole = (role: Role) => {
    this.props.addRole(this.props.repositoryRolesLink, role, () =>
      this.repositoryRoleCreated(role)
    );
  };

  render() {
    return (
      <>
        <RepositoryRoleForm
          disabled={this.props.disabled}
          submitForm={role => this.createRepositoryRole(role)}
        />
      </>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isFetchVerbsPending(state);
  const error = getFetchVerbsFailure(state);
  const verbsLink = getRepositoryVerbsLink(state);
  const repositoryRolesLink = getRepositoryRolesLink(state);

  return {
    loading,
    error,
    verbsLink,
    repositoryRolesLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    addRole: (link: string, role: Role) => {
      dispatch(createRole(link, role));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("roles")(CreateRepositoryRole));

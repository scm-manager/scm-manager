// @flow
import React from "react";
import type {
  AvailableRepositoryPermissions,
  Permission
} from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import {
  modifyPermission,
  isModifyPermissionPending,
  deletePermission,
  isDeletePermissionPending,
  findMatchingRoleName
} from "../modules/permissions";
import { connect } from "react-redux";
import type { History } from "history";
import { Checkbox } from "@scm-manager/ui-components";
import DeletePermissionButton from "../components/buttons/DeletePermissionButton";
import TypeSelector from "../components/TypeSelector";

type Props = {
  availablePermissions: AvailableRepositoryPermissions,
  submitForm: Permission => void,
  modifyPermission: (Permission, string, string) => void,
  permission: Permission,
  t: string => string,
  namespace: string,
  repoName: string,
  match: any,
  history: History,
  loading: boolean,
  deletePermission: (Permission, string, string) => void,
  deleteLoading: boolean
};

type State = {
  role: string,
  permission: Permission
};

class SinglePermission extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    const defaultPermission = props.availablePermissions.availableRoles
      ? props.availablePermissions.availableRoles[0]
      : {};

    this.state = {
      permission: {
        name: "",
        verbs: defaultPermission.verbs,
        groupPermission: false,
        _links: {}
      },
      role: defaultPermission.name
    };
  }

  componentDidMount() {
    const { availablePermissions, permission } = this.props;

    const matchingRole = findMatchingRoleName(
      availablePermissions,
      permission.verbs
    );

    if (permission) {
      this.setState({
        permission: {
          name: permission.name,
          verbs: permission.verbs,
          groupPermission: permission.groupPermission,
          _links: permission._links
        },
        role: matchingRole
      });
    }
  }

  deletePermission = () => {
    this.props.deletePermission(
      this.props.permission,
      this.props.namespace,
      this.props.repoName
    );
  };

  render() {
    const { role, permission } = this.state;
    const { availablePermissions, loading, namespace, repoName } = this.props;
    const availableRoleNames = availablePermissions.availableRoles.map(
      r => r.name
    );
    const typeSelector =
      this.props.permission._links && this.props.permission._links.update ? (
        <td>
          <TypeSelector
            handleTypeChange={this.handleTypeChange}
            availableTypes={availableRoleNames}
            type={role}
            loading={loading}
          />
        </td>
      ) : (
        <td>{role}</td>
      );

    return (
      <tr>
        <td>{permission.name}</td>
        <td>
          <Checkbox checked={permission ? permission.groupPermission : false} />
        </td>
        {typeSelector}
        <td>
          <DeletePermissionButton
            permission={permission}
            namespace={namespace}
            repoName={repoName}
            deletePermission={this.deletePermission}
            loading={this.props.deleteLoading}
          />
        </td>
      </tr>
    );
  }

  handleTypeChange = (type: string) => {
    const selectedRole = this.findAvailableRole(type);
    this.setState({
      permission: {
        ...this.state.permission,
        verbs: selectedRole.verbs
      },
      role: type
    });
    this.modifyPermission(selectedRole.verbs);
  };

  findAvailableRole = (type: string) => {
    return this.props.availablePermissions.availableRoles.find(
      role => role.name === type
    );
  };

  modifyPermission = (verbs: string[]) => {
    let permission = this.state.permission;
    permission.verbs = verbs;
    this.props.modifyPermission(
      permission,
      this.props.namespace,
      this.props.repoName
    );
  };

  createSelectOptions(types: string[]) {
    return types.map(type => {
      return {
        label: type,
        value: type
      };
    });
  }
}

const mapStateToProps = (state, ownProps) => {
  const permission = ownProps.permission;
  const loading = isModifyPermissionPending(
    state,
    ownProps.namespace,
    ownProps.repoName,
    permission
  );
  const deleteLoading = isDeletePermissionPending(
    state,
    ownProps.namespace,
    ownProps.repoName,
    permission
  );

  return { loading, deleteLoading };
};

const mapDispatchToProps = dispatch => {
  return {
    modifyPermission: (
      permission: Permission,
      namespace: string,
      repoName: string
    ) => {
      dispatch(modifyPermission(permission, namespace, repoName));
    },
    deletePermission: (
      permission: Permission,
      namespace: string,
      repoName: string
    ) => {
      dispatch(deletePermission(permission, namespace, repoName));
    }
  };
};
export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(SinglePermission));

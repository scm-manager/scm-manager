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
import { Button } from "@scm-manager/ui-components";
import DeletePermissionButton from "../components/buttons/DeletePermissionButton";
import RoleSelector from "../components/RoleSelector";
import AdvancedPermissionsDialog from "./AdvancedPermissionsDialog";
import classNames from "classnames";
import injectSheet from "react-jss";

type Props = {
  availablePermissions: AvailableRepositoryPermissions,
  submitForm: Permission => void,
  modifyPermission: (
    permission: Permission,
    namespace: string,
    name: string
  ) => void,
  permission: Permission,
  t: string => string,
  namespace: string,
  repoName: string,
  match: any,
  history: History,
  loading: boolean,
  deletePermission: (
    permission: Permission,
    namespace: string,
    name: string
  ) => void,
  deleteLoading: boolean,
  classes: any
};

type State = {
  role: string,
  permission: Permission,
  showAdvancedDialog: boolean
};

const styles = {
  iconColor: {
    color: "#9a9a9a"
  },
  centerMiddle: {
    display: "table-cell",
    verticalAlign: "middle !important"
  },
  columnWidth: {
    width: "100%"
  }
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
      role: defaultPermission.name,
      showAdvancedDialog: false
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
    const { role, permission, showAdvancedDialog } = this.state;
    const {
      t,
      availablePermissions,
      loading,
      namespace,
      repoName,
      classes
    } = this.props;
    const availableRoleNames = availablePermissions.availableRoles.map(
      r => r.name
    );
    const readOnly = !this.mayChangePermissions();
    const roleSelector = readOnly ? (
      <td>{role}</td>
    ) : (
      <td>
        <RoleSelector
          handleRoleChange={this.handleRoleChange}
          availableRoles={availableRoleNames}
          role={role}
          loading={loading}
        />
      </td>
    );

    const advancedDialg = showAdvancedDialog ? (
      <AdvancedPermissionsDialog
        readOnly={readOnly}
        availableVerbs={availablePermissions.availableVerbs}
        selectedVerbs={permission.verbs}
        onClose={this.closeAdvancedPermissionsDialog}
        onSubmit={this.submitAdvancedPermissionsDialog}
      />
    ) : null;

    const iconType =
      permission && permission.groupPermission ? (
        <i title={t("permission.group")} className={classNames("fas fa-user-friends", classes.iconColor)} />
      ) : (
        <i title={t("permission.user")} className={classNames("fas fa-user", classes.iconColor)} />
      );

    return (
      <tr className={classes.columnWidth}>
        <td className={classes.centerMiddle}>
          {iconType} {permission.name}
        </td>
        {roleSelector}
        <td className={classes.centerMiddle}>
          <Button
            label={t("permission.advanced-button.label")}
            action={this.handleDetailedPermissionsPressed}
          />
        </td>
        <td className={classes.centerMiddle}>
          <DeletePermissionButton
            permission={permission}
            namespace={namespace}
            repoName={repoName}
            deletePermission={this.deletePermission}
            loading={this.props.deleteLoading}
          />
          {advancedDialg}
        </td>
      </tr>
    );
  }

  mayChangePermissions = () => {
    return this.props.permission._links && this.props.permission._links.update;
  };

  handleDetailedPermissionsPressed = () => {
    this.setState({ showAdvancedDialog: true });
  };

  closeAdvancedPermissionsDialog = () => {
    this.setState({ showAdvancedDialog: false });
  };

  submitAdvancedPermissionsDialog = (newVerbs: string[]) => {
    const { permission } = this.state;
    const newRole = findMatchingRoleName(
      this.props.availablePermissions,
      newVerbs
    );
    this.setState(
      {
        showAdvancedDialog: false,
        permission: { ...permission, verbs: newVerbs },
        role: newRole
      },
      () => this.modifyPermission(newVerbs)
    );
  };

  handleRoleChange = (role: string) => {
    const selectedRole = this.findAvailableRole(role);
    this.setState(
      {
        permission: {
          ...this.state.permission,
          verbs: selectedRole.verbs
        },
        role: role
      },
      () => this.modifyPermission(selectedRole.verbs)
    );
  };

  findAvailableRole = (roleName: string) => {
    return this.props.availablePermissions.availableRoles.find(
      role => role.name === roleName
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
)(translate("repos")(injectSheet(styles)(SinglePermission)));

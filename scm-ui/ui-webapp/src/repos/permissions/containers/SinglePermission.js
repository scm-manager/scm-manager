// @flow
import React from "react";
import { connect } from "react-redux";
import type { History } from "history";
import { translate } from "react-i18next";
import styled from "styled-components";
import type { RepositoryRole, Permission } from "@scm-manager/ui-types";
import { Button, Icon } from "@scm-manager/ui-components";
import {
  modifyPermission,
  isModifyPermissionPending,
  deletePermission,
  isDeletePermissionPending,
  findVerbsForRole
} from "../modules/permissions";
import DeletePermissionButton from "../components/buttons/DeletePermissionButton";
import RoleSelector from "../components/RoleSelector";
import AdvancedPermissionsDialog from "./AdvancedPermissionsDialog";

type Props = {
  availableRepositoryRoles: RepositoryRole[],
  availableRepositoryVerbs: string[],
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
  deleteLoading: boolean
};

type State = {
  permission: Permission,
  showAdvancedDialog: boolean
};

const FullWidthTr = styled.tr`
  width: 100%;
`;

const VCenteredTd = styled.td`
  display: table-cell;
  vertical-align: middle !important;
`;

class SinglePermission extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    const defaultPermission = props.availableRepositoryRoles
      ? props.availableRepositoryRoles[0]
      : {};

    this.state = {
      permission: {
        name: "",
        role: undefined,
        verbs: defaultPermission.verbs,
        groupPermission: false,
        _links: {}
      },
      showAdvancedDialog: false
    };
  }

  componentDidMount() {
    const { permission } = this.props;

    if (permission) {
      this.setState({
        permission: {
          name: permission.name,
          role: permission.role,
          verbs: permission.verbs,
          groupPermission: permission.groupPermission,
          _links: permission._links
        }
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
    const {
      availableRepositoryRoles,
      availableRepositoryVerbs,
      loading,
      namespace,
      repoName,
      t
    } = this.props;
    const { permission, showAdvancedDialog } = this.state;
    const availableRoleNames =
      !!availableRepositoryRoles && availableRepositoryRoles.map(r => r.name);
    const readOnly = !this.mayChangePermissions();
    const roleSelector = readOnly ? (
      <td>{permission.role ? permission.role : t("permission.custom")}</td>
    ) : (
      <td>
        <RoleSelector
          handleRoleChange={this.handleRoleChange}
          availableRoles={availableRoleNames}
          role={permission.role}
          loading={loading}
        />
      </td>
    );

    const selectedVerbs = permission.role
      ? findVerbsForRole(availableRepositoryRoles, permission.role)
      : permission.verbs;

    const advancedDialog = showAdvancedDialog ? (
      <AdvancedPermissionsDialog
        readOnly={readOnly}
        availableVerbs={availableRepositoryVerbs}
        selectedVerbs={selectedVerbs}
        onClose={this.closeAdvancedPermissionsDialog}
        onSubmit={this.submitAdvancedPermissionsDialog}
      />
    ) : null;

    const iconType =
      permission && permission.groupPermission ? (
        <Icon title={t("permission.group")} name="user-friends" />
      ) : (
        <Icon title={t("permission.user")} name="user" />
      );

    return (
      <FullWidthTr>
        <VCenteredTd>
          {iconType} {permission.name}
        </VCenteredTd>
        {roleSelector}
        <VCenteredTd>
          <Button
            label={t("permission.advanced-button.label")}
            action={this.handleDetailedPermissionsPressed}
          />
        </VCenteredTd>
        <VCenteredTd className="is-darker">
          <DeletePermissionButton
            permission={permission}
            namespace={namespace}
            repoName={repoName}
            deletePermission={this.deletePermission}
            loading={this.props.deleteLoading}
          />
          {advancedDialog}
        </VCenteredTd>
      </FullWidthTr>
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
    this.setState(
      {
        showAdvancedDialog: false,
        permission: { ...permission, role: undefined, verbs: newVerbs }
      },
      () => this.modifyPermissionVerbs(newVerbs)
    );
  };

  handleRoleChange = (role: string) => {
    const { permission } = this.state;
    this.setState(
      {
        permission: { ...permission, role: role, verbs: undefined }
      },
      () => this.modifyPermissionRole(role)
    );
  };

  findAvailableRole = (roleName: string) => {
    const { availableRepositoryRoles } = this.props;
    return availableRepositoryRoles.find(role => role.name === roleName);
  };

  modifyPermissionRole = (role: string) => {
    let permission = this.state.permission;
    permission.role = role;
    this.props.modifyPermission(
      permission,
      this.props.namespace,
      this.props.repoName
    );
  };

  modifyPermissionVerbs = (verbs: string[]) => {
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
)(translate("repos")(SinglePermission));

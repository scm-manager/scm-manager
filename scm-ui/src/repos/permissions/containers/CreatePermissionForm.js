// @flow
import React from "react";
import {translate} from "react-i18next";
import type {PermissionCollection, PermissionCreateEntry, RepositoryRole, SelectValue} from "@scm-manager/ui-types";
import {
  Button,
  GroupAutocomplete,
  LabelWithHelpIcon,
  Radio,
  SubmitButton,
  Subtitle,
  UserAutocomplete
} from "@scm-manager/ui-components";
import * as validator from "../components/permissionValidation";
import RoleSelector from "../components/RoleSelector";
import AdvancedPermissionsDialog from "./AdvancedPermissionsDialog";
import {findVerbsForRole} from "../modules/permissions";

type Props = {
  availableRoles: RepositoryRole[],
  availableVerbs: string[],
  createPermission: (permission: PermissionCreateEntry) => void,
  loading: boolean,
  currentPermissions: PermissionCollection,
  groupAutocompleteLink: string,
  userAutocompleteLink: string,

  // Context props
  t: string => string
};

type State = {
  name: string,
  role?: string,
  verbs?: string[],
  groupPermission: boolean,
  valid: boolean,
  value?: SelectValue,
  showAdvancedDialog: boolean
};

class CreatePermissionForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      name: "",
      role: props.availableRoles[0].name,
      verbs: undefined,
      groupPermission: false,
      valid: true,
      value: undefined,
      showAdvancedDialog: false
    };
  }

  permissionScopeChanged = event => {
    const groupPermission = event.target.value === "GROUP_PERMISSION";
    this.setState({
      value: undefined,
      name: "",
      groupPermission: groupPermission,
      valid: false
    });
  };

  renderAutocompletionField = () => {
    const group = this.state.groupPermission;
    if (group) {
      return (
        <GroupAutocomplete
          autocompleteLink={this.props.groupAutocompleteLink}
          valueSelected={this.selectName}
          value={this.state.value ? this.state.value : ""}
        />
      );
    }
    return (
      <UserAutocomplete
        autocompleteLink={this.props.userAutocompleteLink}
        valueSelected={this.selectName}
        value={this.state.value ? this.state.value : ""}
      />
    );
  };

  selectName = (value: SelectValue) => {
    this.setState({
      value,
      name: value.value.id,
      valid: validator.isPermissionValid(
        value.value.id,
        this.state.groupPermission,
        this.props.currentPermissions
      )
    });
  };

  render() {
    const { t, availableRoles, availableVerbs, loading } = this.props;
    const { role, verbs, showAdvancedDialog } = this.state;

    const availableRoleNames = availableRoles.map(r => r.name);

    const selectedVerbs = role ? findVerbsForRole(availableRoles, role) : verbs;

    const advancedDialog = showAdvancedDialog ? (
      <AdvancedPermissionsDialog
        availableVerbs={availableVerbs}
        selectedVerbs={selectedVerbs}
        onClose={this.toggleAdvancedPermissionsDialog}
        onSubmit={this.submitAdvancedPermissionsDialog}
      />
    ) : null;

    return (
      <>
        <hr />
        <Subtitle
          subtitle={t("permission.add-permission.add-permission-heading")}
        />
        {advancedDialog}
        <form onSubmit={this.submit}>
          <div className="field is-grouped">
            <div className="control">
              <Radio
                name="permission_scope"
                value="USER_PERMISSION"
                checked={!this.state.groupPermission}
                label={t("permission.user-permission")}
                onChange={this.permissionScopeChanged}
              />
              <Radio
                name="permission_scope"
                value="GROUP_PERMISSION"
                checked={this.state.groupPermission}
                label={t("permission.group-permission")}
                onChange={this.permissionScopeChanged}
              />
            </div>
          </div>
          <div className="columns">
            <div className="column is-three-fifths">
              {this.renderAutocompletionField()}
            </div>
            <div className="column is-two-fifths">
              <div className="columns">
                <div className="column is-narrow">
                  <RoleSelector
                    availableRoles={availableRoleNames}
                    label={t("permission.role")}
                    helpText={t("permission.help.roleHelpText")}
                    handleRoleChange={this.handleRoleChange}
                    role={role}
                  />
                </div>
                <div className="column">
                  <LabelWithHelpIcon
                    label={t("permission.permissions")}
                    helpText={t("permission.help.permissionsHelpText")}
                  />
                  <Button
                    label={t("permission.advanced-button.label")}
                    action={this.toggleAdvancedPermissionsDialog}
                  />
                </div>
              </div>
            </div>
          </div>
          <div className="columns">
            <div className="column">
              <SubmitButton
                label={t("permission.add-permission.submit-button")}
                loading={loading}
                disabled={!this.state.valid || this.state.name === ""}
              />
            </div>
          </div>
        </form>
      </>
    );
  }

  toggleAdvancedPermissionsDialog = () => {
    this.setState(prevState => ({
      showAdvancedDialog: !prevState.showAdvancedDialog
    }));
  };

  submitAdvancedPermissionsDialog = (newVerbs: string[]) => {
    this.setState({
      showAdvancedDialog: false,
      role: undefined,
      verbs: newVerbs
    });
  };

  submit = e => {
    this.props.createPermission({
      name: this.state.name,
      role: this.state.role,
      verbs: this.state.verbs,
      groupPermission: this.state.groupPermission
    });
    this.removeState();
    e.preventDefault();
  };

  removeState = () => {
    this.setState({
      name: "",
      role: this.props.availableRoles[0].name,
      verbs: undefined,
      valid: true,
      value: undefined
    });
  };

  handleRoleChange = (role: string) => {
    const selectedRole = this.findAvailableRole(role);
    if (!selectedRole) {
      return;
    }
    this.setState({
      role: selectedRole.name,
      verbs: []
    });
  };

  findAvailableRole = (roleName: string) => {
    return this.props.availableRoles.find(role => role.name === roleName);
  };
}

export default translate("repos")(CreatePermissionForm);

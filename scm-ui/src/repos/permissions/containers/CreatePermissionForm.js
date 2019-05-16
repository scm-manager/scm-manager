// @flow
import React from "react";
import { translate } from "react-i18next";
import type {
  RepositoryRole,
  PermissionCollection,
  PermissionCreateEntry,
  SelectValue
} from "@scm-manager/ui-types";
import {
  Autocomplete,
  SubmitButton,
  Button,
  LabelWithHelpIcon,
  Radio
} from "@scm-manager/ui-components";
import * as validator from "../components/permissionValidation";
import RoleSelector from "../components/RoleSelector";
import AdvancedPermissionsDialog from "./AdvancedPermissionsDialog";
import { findVerbsForRole } from "../modules/permissions";

type Props = {
  availableRoles: RepositoryRole[],
  availableVerbs: string[],
  createPermission: (permission: PermissionCreateEntry) => void,
  loading: boolean,
  currentPermissions: PermissionCollection,
  groupAutoCompleteLink: string,
  userAutoCompleteLink: string,

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

  loadUserAutocompletion = (inputValue: string) => {
    return this.loadAutocompletion(this.props.userAutoCompleteLink, inputValue);
  };

  loadGroupAutocompletion = (inputValue: string) => {
    return this.loadAutocompletion(
      this.props.groupAutoCompleteLink,
      inputValue
    );
  };

  loadAutocompletion(url: string, inputValue: string) {
    const link = url + "?q=";
    return fetch(link + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map(element => {
          const label = element.displayName
            ? `${element.displayName} (${element.id})`
            : element.id;
          return {
            value: element,
            label
          };
        });
      });
  }

  renderAutocompletionField = () => {
    const { t } = this.props;
    if (this.state.groupPermission) {
      return (
        <Autocomplete
          loadSuggestions={this.loadGroupAutocompletion}
          valueSelected={this.groupOrUserSelected}
          value={this.state.value ? this.state.value : ""}
          label={t("permission.group")}
          noOptionsMessage={t("permission.autocomplete.no-group-options")}
          loadingMessage={t("permission.autocomplete.loading")}
          placeholder={t("permission.autocomplete.group-placeholder")}
          creatable={true}
        />
      );
    }
    return (
      <Autocomplete
        loadSuggestions={this.loadUserAutocompletion}
        valueSelected={this.groupOrUserSelected}
        value={this.state.value ? this.state.value : ""}
        label={t("permission.user")}
        noOptionsMessage={t("permission.autocomplete.no-user-options")}
        loadingMessage={t("permission.autocomplete.loading")}
        placeholder={t("permission.autocomplete.user-placeholder")}
        creatable={true}
      />
    );
  };

  groupOrUserSelected = (value: SelectValue) => {
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
        onClose={this.closeAdvancedPermissionsDialog}
        onSubmit={this.submitAdvancedPermissionsDialog}
      />
    ) : null;

    return (
      <div>
        <hr />
        <h2 className="subtitle">
          {t("permission.add-permission.add-permission-heading")}
        </h2>
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
                    action={this.handleDetailedPermissionsPressed}
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
      </div>
    );
  }

  handleDetailedPermissionsPressed = () => {
    this.setState({ showAdvancedDialog: true });
  };

  closeAdvancedPermissionsDialog = () => {
    this.setState({ showAdvancedDialog: false });
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

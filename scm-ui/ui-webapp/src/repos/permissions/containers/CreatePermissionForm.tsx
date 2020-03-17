/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { PermissionCollection, PermissionCreateEntry, RepositoryRole, SelectValue } from "@scm-manager/ui-types";
import {
  Button,
  GroupAutocomplete,
  LabelWithHelpIcon,
  Level,
  Radio,
  SubmitButton,
  Subtitle,
  UserAutocomplete
} from "@scm-manager/ui-components";
import * as validator from "../components/permissionValidation";
import RoleSelector from "../components/RoleSelector";
import AdvancedPermissionsDialog from "./AdvancedPermissionsDialog";
import { findVerbsForRole } from "../modules/permissions";

type Props = WithTranslation & {
  availableRoles: RepositoryRole[];
  availableVerbs: string[];
  createPermission: (permission: PermissionCreateEntry) => void;
  loading: boolean;
  currentPermissions: PermissionCollection;
  groupAutocompleteLink: string;
  userAutocompleteLink: string;
};

type State = {
  name: string;
  role?: string;
  verbs?: string[];
  groupPermission: boolean;
  valid: boolean;
  value?: SelectValue;
  showAdvancedDialog: boolean;
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

  groupPermissionScopeChanged = (value: boolean) => {
    if (value) {
      this.permissionScopeChanged(true);
    }
  };

  userPermissionScopeChanged = (value: boolean) => {
    if (value) {
      this.permissionScopeChanged(false);
    }
  };

  permissionScopeChanged = (groupPermission: boolean) => {
    this.setState({
      value: undefined,
      name: "",
      groupPermission,
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
      valid: validator.isPermissionValid(value.value.id, this.state.groupPermission, this.props.currentPermissions)
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
        <Subtitle subtitle={t("permission.add-permission.add-permission-heading")} />
        {advancedDialog}
        <form onSubmit={this.submit}>
          <div className="field is-grouped">
            <div className="control">
              <Radio
                name="permission_scope"
                value="USER_PERMISSION"
                checked={!this.state.groupPermission}
                label={t("permission.user-permission")}
                onChange={this.userPermissionScopeChanged}
              />
              <Radio
                name="permission_scope"
                value="GROUP_PERMISSION"
                checked={this.state.groupPermission}
                label={t("permission.group-permission")}
                onChange={this.groupPermissionScopeChanged}
              />
            </div>
          </div>
          <div className="columns">
            <div className="column is-half">{this.renderAutocompletionField()}</div>
            <div className="column is-half">
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
                  <Button label={t("permission.advanced-button.label")} action={this.toggleAdvancedPermissionsDialog} />
                </div>
              </div>
            </div>
          </div>
          <Level
            right={
              <SubmitButton
                label={t("permission.add-permission.submit-button")}
                loading={loading}
                disabled={!this.state.valid || this.state.name === ""}
              />
            }
          />
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

export default withTranslation("repos")(CreatePermissionForm);

// @flow
import React from "react";
import {translate} from "react-i18next";
import {Checkbox, InputField, SubmitButton} from "@scm-manager/ui-components";
import TypeSelector from "./TypeSelector";
import type {PermissionCollection, PermissionCreateEntry} from "@scm-manager/ui-types";
import * as validator from "./permissionValidation";

type Props = {
  t: string => string,
  createPermission: (permission: PermissionCreateEntry) => void,
  loading: boolean,
  currentPermissions: PermissionCollection
};

type State = {
  name: string,
  type: string,
  groupPermission: boolean,
  valid: boolean
};

class CreatePermissionForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      name: "",
      type: "READ",
      groupPermission: false,
      valid: true
    };
  }

  render() {
    const { t, loading } = this.props;
    const { name, type, groupPermission } = this.state;

    return (
      <div>
        <h2 className="subtitle">
          {t("permission.add-permission.add-permission-heading")}
        </h2>
        <form onSubmit={this.submit}>
          <InputField
            label={t("permission.name")}
            value={name ? name : ""}
            onChange={this.handleNameChange}
            validationError={!this.state.valid}
            errorMessage={t("permission.add-permission.name-input-invalid")}
          />
          <Checkbox
            label={t("permission.group-permission")}
            checked={groupPermission ? groupPermission : false}
            onChange={this.handleGroupPermissionChange}
          />
          <TypeSelector
            label={t("permission.type")}
            handleTypeChange={this.handleTypeChange}
            type={type ? type : "READ"}
          />
          <SubmitButton
            label={t("permission.add-permission.submit-button")}
            loading={loading}
            disabled={!this.state.valid || this.state.name === ""}
          />
        </form>
      </div>
    );
  }

  submit = e => {
    this.props.createPermission({
      name: this.state.name,
      type: this.state.type,
      groupPermission: this.state.groupPermission
    });
    this.removeState();
    e.preventDefault();
  };

  removeState = () => {
    this.setState({
      name: "",
      type: "READ",
      groupPermission: false,
      valid: true
    });
  };

  handleTypeChange = (type: string) => {
    this.setState({
      type: type
    });
  };

  handleNameChange = (name: string) => {
    this.setState({
      name: name,
      valid: validator.isPermissionValid(
        name,
        this.state.groupPermission,
        this.props.currentPermissions
      )
    });
  };
  handleGroupPermissionChange = (groupPermission: boolean) => {
    this.setState({
      groupPermission: groupPermission,
      valid: validator.isPermissionValid(
        this.state.name,
        groupPermission,
        this.props.currentPermissions
      )
    });
  };
}

export default translate("repos")(CreatePermissionForm);

// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField, SubmitButton } from "@scm-manager/ui-components";
import TypeSelector from "./TypeSelector";
import type {
  PermissionCollection,
  PermissionEntry
} from "@scm-manager/ui-types";

type Props = {
  t: string => string,
  createPermission: (permission: PermissionEntry) => void,
  loading: boolean,
  currentPermissions: PermissionCollection
};

type State = {
  name: string,
  type: string,
  groupPermission: boolean
};

class CreatePermissionForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      name: "",
      type: "",
      groupPermission: false
    };
  }

  render() {
    const { t, loading } = this.props;
    const { name, type, groupPermission } = this.state;

    return (
      <div>
        <h2 className="subtitle">
          {t("add-permission.add-permission-heading")}
        </h2>
        <form onSubmit={this.submit}>
          <InputField
            label={t("permission.name")}
            value={name ? name : ""}
            onChange={this.handleNameChange}
            validationError={this.currentPermissionIncludeName()}
            errorMessage={t("add-permission.name-input-invalid")}
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
            label={t("add-permission.submit-button")}
            loading={loading}
            disabled={this.isValid()}
          />
        </form>
      </div>
    );
  }

  isValid = () => {
    if (
      this.state.name === null ||
      this.state.name === "" ||
      this.currentPermissionIncludeName()
    ) {
      return true;
    }
    return false;
  };

  currentPermissionIncludeName = () => {
    for (let i = 0; i < this.props.currentPermissions.length; i++) {
      if (this.props.currentPermissions[i].name === this.state.name)
        return true;
    }
    return false;
  };

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
      type: "",
      groupPermission: false
    });
  };

  handleTypeChange = (type: string) => {
    this.setState({
      type: type
    });
  };

  handleNameChange = (name: string) => {
    this.setState({
      name: name
    });
  };
  handleGroupPermissionChange = (groupPermission: boolean) => {
    this.setState({
      groupPermission: groupPermission
    });
  };
}

export default translate("permissions")(CreatePermissionForm);

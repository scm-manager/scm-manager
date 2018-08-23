// @flow
import React from "react";
import type { Permission } from "../../types/Permissions";
import { Checkbox, InputField } from "../../../components/forms/index";
import { DeleteButton, SubmitButton } from "../../../components/buttons/index";
import { translate } from "react-i18next";
import { Select } from "../../../components/forms/index";

type Props = {
  submitForm: Permission => void,
  permission: Permission,
  t: string => string
};

type State = {
  permission: Permission
};

class PermissionRowEditable extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      permission: {
        name: "",
        type: "READ",
        groupPermission: false,
        _links: {}
      }
    };
  }

  componentDidMount() {
    const { permission } = this.props;
    if (permission) {
      this.setState({
        permission: {
          name: permission.name,
          type: permission.type,
          groupPermission: permission.groupPermission,
          _links: permission._links
        }
      });
    }
  }
  submit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state.permission);
  };
  render() {
    const { permission } = this.state;
    const { t } = this.props;
    const types = ["READ", "OWNER", "GROUP"];

    const deleteButton = this.props.permission._links.delete ? (
      <DeleteButton label={t("edit-permission.delete-button")} />
    ) : null;

    return (
      <tr>
        <td>
          <InputField
            value={permission.name ? permission.name : ""}
            onChange={this.handleNameChange}
          />
        </td>
        <td className="is-hidden-mobile">
          <Select
            onChange={this.handleTypeChange}
            value={permission.type ? permission.type : ""}
            options={this.createSelectOptions(types)}
          />
        </td>
        <td>
          <Checkbox
            checked={
              permission.groupPermission ? permission.groupPermission : false
            }
            onChange={this.handleGroupPermissionChange}
          />
        </td>
        <td>
          <SubmitButton
            label={t("edit-permission.save-button")}
            action={this.submit}
          />
        </td>
        <td>{deleteButton}</td>
      </tr>
    );
  }

  handleTypeChange = (type: string) => {
    this.setState({
      permission: {
        ...this.state.permission,
        type: type
      }
    });
  };

  createSelectOptions(types: string[]) {
    return types.map(type => {
      return {
        label: type,
        value: type
      };
    });
  }

  handleNameChange = (name: string) => {
    this.setState({
      permission: {
        ...this.state.permission,
        name: name
      }
    });
  };

  handleGroupPermissionChange = (groupPermission: boolean) => {
    this.setState({
      permission: {
        ...this.state.permission,
        groupPermission: groupPermission
      }
    });
  };
}

export default translate("permissions")(PermissionRowEditable);

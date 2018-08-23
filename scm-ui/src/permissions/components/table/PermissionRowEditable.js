// @flow
import React from "react";
import type { Permission } from "../../types/Permissions";
import { Checkbox, InputField } from "../../../components/forms/index";
import { DeleteButton, SubmitButton } from "../../../components/buttons/index";
import { translate } from "react-i18next";
import { Select } from "../../../components/forms";

type Props = {
  permission: Permission,
  t: string => string
};

type State = {
  name: string,
  type: string,
  groupPermission: boolean
};

class PermissionRowEditable extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      name: "",
      type: "READ",
      groupPermission: false
    };
  }

  componentDidMount() {
    const { permission } = this.props;
    if (permission) {
      this.setState({
        name: permission.name,
        type: permission.type,
        groupPermission: permission.groupPermission
      });
    }
  }

  render() {
    const { name, type, groupPermission } = this.state;
    const { t } = this.props;
    const types = ["READ", "OWNER", "GROUP"];

    const deleteButton = this.props.permission._links.delete ? (
      <DeleteButton label={t("edit-permission.delete-button")} />
    ) : null;

    return (
      <tr>
        <td>
          <InputField
            value={name ? name : ""}
            onChange={this.handleNameChange}
          />
        </td>
        <td className="is-hidden-mobile">
          <Select
            onChange={this.handleTypeChange}
            value={type ? type : ""}
            options={this.createSelectOptions(types)}
          />
        </td>
        <td>
          <Checkbox
            checked={groupPermission ? groupPermission : false}
            onChange={this.handleGroupPermissionChange}
          />
        </td>
        <td>
          <SubmitButton label={t("edit-permission.save-button")} />
        </td>
        <td>{deleteButton}</td>
      </tr>
    );
  }

  handleTypeChange = (type: string) => {
    this.setState({
      type: type
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
      name: name
    });
  };

  handleGroupPermissionChange = (groupPermission: boolean) => {
    this.setState({
      groupPermission: groupPermission
    });
  };
}

export default translate("permissions")(PermissionRowEditable);

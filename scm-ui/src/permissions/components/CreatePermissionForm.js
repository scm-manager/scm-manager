// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField } from "../../components/forms";
import TypeSelector from "./TypeSelector";
import type {PermissionEntry} from "../types/Permissions";
import { SubmitButton } from "../../components/buttons";

type Props = {
  t: string => string,
  createPermission: (permission: PermissionEntry) => void,
  loading: boolean
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
      type: "READ",
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
        <table className="table">
          <tbody>
            <tr>
              <td>{t("permission.name")}</td>
              <td>
                <InputField
                  value={name ? name : ""}
                  onChange={this.handleNameChange}
                />
              </td>
            </tr>
            <tr>
              <td>{t("permission.group-permission")}</td>
              <td>
                <Checkbox
                  checked={groupPermission ? groupPermission : false}
                  onChange={this.handleGroupPermissionChange}
                />
              </td>
            </tr>
            <tr>
              <td>{t("permission.type")}</td>
              <td>
                <TypeSelector
                  handleTypeChange={this.handleTypeChange}
                  type={type ? type : "READ"}
                />
              </td>
            </tr>
          </tbody>
        </table>
        <SubmitButton
          label={t("add-permission.submit-button")}
          action={this.submit}
          loading={loading}
        />
      </div>
    );
  }

  submit = () => {
    this.props.createPermission({
      name: this.state.name,
      type: this.state.type,
      groupPermission: this.state.groupPermission
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

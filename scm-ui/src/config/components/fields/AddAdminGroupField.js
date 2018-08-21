//@flow
import React from "react";

import { translate } from "react-i18next";
import { AddButton } from "../../../components/buttons";
import InputField from "../../../components/forms/InputField";

type Props = {
  t: string => string,
  addGroup: string => void,
  disabled: boolean
};

type State = {
  groupToAdd: string
};

class AddAdminGroupField extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      groupToAdd: ""
    };
  }

  render() {
    const { t, disabled } = this.props;
    return (
      <div className="field">
        <InputField
          label={t("admin-settings.add-group-textfield")}
          errorMessage={t("admin-settings.add-group-error")}
          onChange={this.handleAddGroupChange}
          validationError={false}
          value={this.state.groupToAdd}
          onReturnPressed={this.appendGroup}
          disabled={disabled}
        />
        <AddButton
          label={t("admin-settings.add-group-button")}
          action={this.addButtonClicked}
          disabled={disabled}
        />
      </div>
    );
  }

  addButtonClicked = (event: Event) => {
    event.preventDefault();
    this.appendGroup();
  };

  appendGroup = () => {
    const { groupToAdd } = this.state;
    this.props.addGroup(groupToAdd);
    this.setState({ ...this.state, groupToAdd: "" });
  };

  handleAddGroupChange = (groupname: string) => {
    this.setState({
      ...this.state,
      groupToAdd: groupname
    });
  };
}

export default translate("config")(AddAdminGroupField);

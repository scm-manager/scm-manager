//@flow
import React from "react";

import { translate } from "react-i18next";
import { AddButton } from "../../../components/buttons";
import InputField from "../../../components/forms/InputField";

type Props = {
  t: string => string,
  addUser: string => void,
  disabled: boolean
};

type State = {
  userToAdd: string
  //validationError: boolean
};

class AddAdminUserField extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      userToAdd: ""
      //validationError: false
    };
  }

  render() {
    const { t, disabled } = this.props;
    return (
      <div className="field">
        <InputField
          label={t("admin-settings.add-user-textfield")}
          errorMessage={t("admin-settings.add-user-error")}
          onChange={this.handleAddUserChange}
          validationError={false}
          value={this.state.userToAdd}
          onReturnPressed={this.appendUser}
          disabled={disabled}
        />
        <AddButton
          label={t("admin-settings.add-user-button")}
          action={this.addButtonClicked}
          disabled={disabled}
          //disabled={!isMemberNameValid(this.state.memberToAdd)}
        />
      </div>
    );
  }

  addButtonClicked = (event: Event) => {
    event.preventDefault();
    this.appendUser();
  };

  appendUser = () => {
    const { userToAdd } = this.state;
    //if (isMemberNameValid(memberToAdd)) {
    this.props.addUser(userToAdd);
    this.setState({ ...this.state, userToAdd: "" });
    // }
  };

  handleAddUserChange = (username: string) => {
    this.setState({
      ...this.state,
      userToAdd: username
      //validationError: membername.length > 0 && !isMemberNameValid(membername)
    });
  };
}

export default translate("config")(AddAdminUserField);

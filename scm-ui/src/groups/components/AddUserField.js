//@flow
import React from "react";

import { translate } from "react-i18next";
import AddButton from "../../components/buttons/AddButton";
import InputField from "../../components/forms/InputField";
import { isMemberNameValid } from "./groupValidation"

type Props = {
  t: string => string,
  addUser: string => void
};

type State = {
  userToAdd: string,
  validationError: boolean
};

class AddUserField extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      userToAdd: "",
      validationError: false
    };
  }

  render() {
    const { t } = this.props;
    return (
      <div className="field">
        <InputField
          label={t("add-user-textfield.label")}
          errorMessage={t("add-user-textfield.error")}
          onChange={this.handleAddUserChange}
          validationError={this.state.validationError}
          value={this.state.userToAdd}
          onReturnPressed={this.appendMember}
        />
        <AddButton

          label={t("add-user-button.label")}
          action={this.addButtonClicked}
        />
      </div>
    );
  }


  addButtonClicked = (event: Event) => {
    event.preventDefault();
    this.appendMember();
  };

  appendMember = () => {
    this.props.addUser(this.state.userToAdd);
    this.setState({ ...this.state, userToAdd: "" });
  }

  handleAddUserChange = (username: string) => {
    this.setState({ ...this.state, userToAdd: username, validationError: !isMemberNameValid(username)});
  };
}

export default translate("groups")(AddUserField);

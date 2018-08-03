//@flow
import React from "react";

import { translate } from "react-i18next";
import AddButton from "../../components/buttons/AddButton";
import InputField from "../../components/forms/InputField";

type Props = {
  t: string => string,
  addUser: string => void
};

type State = {
  userToAdd: string
};

class AddUserField extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      userToAdd: ""
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
          validationError={false} //TODO: validate user name
          value={this.state.userToAdd}
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
    this.props.addUser(this.state.userToAdd);
    this.setState({ ...this.state, userToAdd: "" });
  };

  handleAddUserChange = (username: string) => {
    this.setState({ ...this.state, userToAdd: username });
  };
}

export default translate("groups")(AddUserField);

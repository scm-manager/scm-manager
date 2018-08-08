//@flow
import React from "react";

import { translate } from "react-i18next";
import { AddButton } from "../../components/buttons";
import InputField from "../../components/forms/InputField";
import { isMemberNameValid } from "./groupValidation";

type Props = {
  t: string => string,
  addMember: string => void
};

type State = {
  memberToAdd: string,
  validationError: boolean
};

class AddMemberField extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      memberToAdd: "",
      validationError: false
    };
  }

  render() {
    const { t } = this.props;
    return (
      <div className="field">
        <InputField
          label={t("add-member-textfield.label")}
          errorMessage={t("add-member-textfield.error")}
          onChange={this.handleAddMemberChange}
          validationError={this.state.validationError}
          value={this.state.memberToAdd}
          onReturnPressed={this.appendMember}
        />
        <AddButton
          label={t("add-member-button.label")}
          action={this.addButtonClicked}
          disabled={!isMemberNameValid(this.state.memberToAdd)}
        />
      </div>
    );
  }

  addButtonClicked = (event: Event) => {
    event.preventDefault();
    this.appendMember();
  };

  appendMember = () => {
    const { memberToAdd } = this.state;
    if (isMemberNameValid(memberToAdd)) {
      this.props.addMember(memberToAdd);
      this.setState({ ...this.state, memberToAdd: "" });
    }
  };

  handleAddMemberChange = (membername: string) => {
    this.setState({
      ...this.state,
      memberToAdd: membername,
      validationError: membername.length > 0 && !isMemberNameValid(membername)
    });
  };
}

export default translate("groups")(AddMemberField);

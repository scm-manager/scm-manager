//@flow
import React from "react";

import { translate } from "react-i18next";
import AddButton from "../../components/buttons/AddButton";
import InputField from "../../components/forms/InputField";

type Props = {
  t: string => string,
  addMember: string => void
};

type State = {
  memberToAdd: string
};

class AddMemberField extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      memberToAdd: ""
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
          validationError={false} //TODO: validate member name
          value={this.state.memberToAdd}
        />
        <AddButton
          label={t("add-member-button.label")}
          action={this.addButtonClicked}
        />
      </div>
    );
  }

  addButtonClicked = (event: Event) => {
    event.preventDefault();
    this.props.addMember(this.state.memberToAdd);
    this.setState({ ...this.state, memberToAdd: "" });
  };

  handleAddMemberChange = (membername: string) => {
    this.setState({ ...this.state, memberToAdd: membername });
  };
}

export default translate("groups")(AddMemberField);

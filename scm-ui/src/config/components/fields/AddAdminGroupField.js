//@flow
import React from "react";

import { translate } from "react-i18next";
import { AddButton } from "../../../components/buttons";
import InputField from "../../../components/forms/InputField";

type Props = {
  t: string => string,
  addGroup: string => void
};

type State = {
  groupToAdd: string,
  //validationError: boolean
};

class AddAdminGroupField extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      groupToAdd: "",
      //validationError: false
    };
  }

  render() {
    const { t } = this.props;
    return (
      <div className="field">
        <InputField

          label={t("admin-settings.add-group-textfield")}
          errorMessage={t("admin-settings.add-group-error")}
          onChange={this.handleAddGroupChange}
          validationError={false}
          value={this.state.groupToAdd}
          onReturnPressed={this.appendGroup}
        />
        <AddButton
          label={t("admin-settings.add-group-button")}
          action={this.addButtonClicked}
          //disabled={!isMemberNameValid(this.state.memberToAdd)}
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
    //if (isMemberNameValid(memberToAdd)) {
      this.props.addGroup(groupToAdd);
      this.setState({ ...this.state, groupToAdd: "" });
   // }
  };

  handleAddGroupChange = (groupname: string) => {
    this.setState({
      ...this.state,
      groupToAdd: groupname,
      //validationError: membername.length > 0 && !isMemberNameValid(membername)
    });
  };
}

export default translate("config")(AddAdminGroupField);

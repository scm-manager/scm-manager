//@flow
import React from "react";

import InputField from "../../components/forms/InputField";
import { SubmitButton } from "../../components/buttons";
import { translate } from "react-i18next";
import type { Group } from "../types/Group";
import * as validator from "./groupValidation";

type Props = {
  t: string => string,
  submitForm: Group => void,
  loading?: boolean
};

type State = {
  group: Group,
  nameValidationError: boolean
};

class GroupForm extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      group: {
        name: "",
        description: "",
        _embedded: {
          members: []
        },
        _links: {},
        members: [],
        type: ""
      },
      nameValidationError: false
    };
  }

  onSubmit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state.group);
  };

  isValid = () => {
    const group = this.state.group;
    return !(this.state.nameValidationError || group.name);
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid) {
      this.props.submitForm(this.state.group);
    }
  };

  render() {
    const { t, loading } = this.props;
    return (
      <form onSubmit={this.onSubmit}>
        <InputField
          label={t("group.name")}
          errorMessage="group name invalid"
          onChange={this.handleGroupNameChange}
          validationError={this.state.nameValidationError}
        />
        <InputField
          label={t("group.description")}
          errorMessage=""
          onChange={this.handleDescriptionChange}
          validationError={false}
        />
          <SubmitButton label={t("group-form.submit")} loading={loading}/>
      </form>
    );
  }

  handleGroupNameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isNameValid(name),
      group: { ...this.state.group, name }
    });
  };

  handleDescriptionChange = (description: string) => {
    this.setState({
      group: { ...this.state.group, description }
    });
  };
}

export default translate("groups")(GroupForm);

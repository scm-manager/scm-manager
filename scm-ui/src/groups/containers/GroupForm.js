//@flow
import React from "react";

import InputField from "../../components/forms/InputField";
import { SubmitButton } from "../../components/buttons";
import { translate } from "react-i18next";
import type { Group } from "../types/Group";

export interface Props {
  t: string => string;
  submitForm: Group => void;
}

export interface State {
  group: Group;
}

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
        type: "",
      }
    };
  }
  onSubmit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state.group);
  };

  isValid = () => {
    return true;
  }

  submit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state.group)
  }

  render() {
    const { t } = this.props;
    return (
      <form onSubmit={this.onSubmit}>
        <InputField
          label={t("group.name")}
          errorMessage=""
          onChange={this.handleGroupNameChange}
          validationError={false}
        />
        <InputField
          label={t("group.description")}
          errorMessage=""
          onChange={this.handleDescriptionChange}
          validationError={false}
        />
        <SubmitButton label={t("group-form.submit")} />
      </form>
    );
  }

  handleGroupNameChange = (name: string) => {
    this.setState({
      group: {
        ...this.state.group,
        name
      }
    });
  };

  handleDescriptionChange = (description: string) => {
    this.setState({
      group: {
        ...this.state.group,
        description
      }
    });
  };
}

export default translate("groups")(GroupForm);

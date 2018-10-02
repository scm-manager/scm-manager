//@flow
import React from "react";
import { translate } from "react-i18next";
import {
  InputField,
  SubmitButton,
  Textarea,
  AddEntryToTableField
} from "@scm-manager/ui-components";
import type { Group } from "@scm-manager/ui-types";

import * as validator from "./groupValidation";
import MemberNameTable from "./MemberNameTable";

type Props = {
  t: string => string,
  submitForm: Group => void,
  loading?: boolean,
  group?: Group
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

  componentDidMount() {
    const { group } = this.props;
    if (group) {
      this.setState({ ...this.state, group: { ...group } });
    }
  }

  isFalsy(value) {
    if (!value) {
      return true;
    }
    return false;
  }

  isValid = () => {
    const group = this.state.group;
    return !(this.state.nameValidationError || this.isFalsy(group.name));
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.submitForm(this.state.group);
    }
  };

  render() {
    const { t, loading } = this.props;
    const group = this.state.group;
    let nameField = null;
    if (!this.props.group) {
      nameField = (
        <InputField
          label={t("group.name")}
          errorMessage={t("group-form.name-error")}
          onChange={this.handleGroupNameChange}
          value={group.name}
          validationError={this.state.nameValidationError}
          helpText={t("group-form.help.nameHelpText")}
        />
      );
    }

    return (
      <form onSubmit={this.submit}>
        {nameField}
        <Textarea
          label={t("group.description")}
          errorMessage={t("group-form.description-error")}
          onChange={this.handleDescriptionChange}
          value={group.description}
          validationError={false}
          helpText={t("group-form.help.descriptionHelpText")}
        />
        <MemberNameTable
          members={this.state.group.members}
          memberListChanged={this.memberListChanged}
        />
        <AddEntryToTableField
          addEntry={this.addMember}
          disabled={false}
          buttonLabel={t("add-member-button.label")}
          fieldLabel={t("add-member-textfield.label")}
          errorMessage={t("add-member-textfield.error")}
        />
        <SubmitButton
          disabled={!this.isValid()}
          label={t("group-form.submit")}
          loading={loading}
        />
      </form>
    );
  }

  memberListChanged = membernames => {
    this.setState({
      ...this.state,
      group: {
        ...this.state.group,
        members: membernames
      }
    });
  };

  addMember = (membername: string) => {
    if (this.isMember(membername)) {
      return;
    }

    this.setState({
      ...this.state,
      group: {
        ...this.state.group,
        members: [...this.state.group.members, membername]
      }
    });
  };

  isMember = (membername: string) => {
    return this.state.group.members.includes(membername);
  };

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

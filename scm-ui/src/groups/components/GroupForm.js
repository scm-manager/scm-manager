//@flow
import React from "react";

import InputField from "../../components/forms/InputField";
import { SubmitButton, Button } from "../../components/buttons";
import { translate } from "react-i18next";
import type { Group } from "../types/Group";
import * as validator from "./groupValidation";
import AddUserField from "./AddUserField";
import UserNameTable from "./UserNameTable";

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
    return !(
      this.state.nameValidationError ||
      this.isFalsy(group.name) ||
      this.isFalsy(group.description)
    );
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
        />
      );
    }

    return (
      <form onSubmit={this.submit}>
        {nameField}
        <InputField
          label={t("group.description")}
          errorMessage={t("group-form.description-error")}
          onChange={this.handleDescriptionChange}
          value={group.description}
          validationError={false}
        />
        <UserNameTable
          users={this.state.group.members}
          userListChanged={this.userListChanged}
        />
        <AddUserField addUser={this.addUser} />
        <SubmitButton
          disabled={!this.isValid()}
          label={t("group-form.submit")}
          loading={loading}
        />
      </form>
    );
  }

  userListChanged = usernames => {
    this.setState({
      ...this.state,
      group: {
        ...this.state.group,
        members: usernames
      }
    });
  }


  addUser = (username: string) => {
    if (this.isMember(username)) {
      return;
    }

    this.setState({
      ...this.state,
      group: {
        ...this.state.group,
        members: [...this.state.group.members, username]
      }
    });
  };

  isMember = (username: string) => {
    return this.state.group.members.includes(username);
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

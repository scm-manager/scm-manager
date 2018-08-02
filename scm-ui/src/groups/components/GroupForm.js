//@flow
import React from "react";

import InputField from "../../components/forms/InputField";
import { SubmitButton, Button } from "../../components/buttons";
import { translate } from "react-i18next";
import type { Group } from "../types/Group";
import * as validator from "./groupValidation";

type Props = {
  t: string => string,
  submitForm: Group => void,
  loading?: boolean,
  group?: Group
};

type State = {
  group: Group,
  userToAdd: string,
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
      nameValidationError: false,
      userToAdd: ""
    };
  }

  componentDidMount() {
    const { group } = this.props
    if (group) {
      this.setState({group: {...group}})
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
    const group = this.state.group
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
        <label className="label">{t("group.members")}</label>
        <table className="table is-hoverable is-fullwidth">
        <tbody>
          {this.state.group.members.map((user, index) => {
            return <tr key={user}>
            <td key={user}>{user}</td>
            <td><Button label="Remove" action={this.removeUser.bind(this, user)} key={user}/></td>
            </tr>
          })}
        </tbody>
      </table>
        <InputField
          label="Add user"
          errorMessage="Error"
          onChange={this.handleAddUserChange}
          validationError={false} 
          value={this.state.userToAdd}/>
        
          <Button label="Add user" action={this.addUserClick} />

          <SubmitButton disabled={!this.isValid()} label={t("group-form.submit")} loading={loading}/>
      </form>
    );
  }

removeUser(user: string, event: Event) {
    event.preventDefault();
    let newMembers = this.state.group.members.filter(name => name !== user)
    this.setState({...this.state, group: {
      ...this.state.group,
      members: newMembers}
    })
  }



  addUserClick = (event: Event) => {
    event.preventDefault();
    this.setState({
      ...this.state,
      userToAdd: "",
      group: {
        ...this.state.group,
        members: [...this.state.group.members, this.state.userToAdd]}
    })
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

  handleAddUserChange = (username: string) => {
    this.setState({
      ...this.state,
      userToAdd: username
    })
  }
}

export default translate("groups")(GroupForm);

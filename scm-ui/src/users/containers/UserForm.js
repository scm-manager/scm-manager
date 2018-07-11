// @flow
import React from "react";
import type { User } from "../types/User";
import InputField from "../../components/InputField";
import Checkbox from "../../components/Checkbox";
import SubmitButton from "../../components/SubmitButton";

type Props = {
  submitForm: User => void,
  user?: User
};

class UserForm extends React.Component<Props, User> {
  submit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state);
  };

  render() {
    const { submitForm, user } = this.props;
    return (
      <div className="container">
        <form onSubmit={this.submit}>
          <InputField
            label="Username"
            onChange={this.handleUsernameChange}
            value={user !== undefined ? user.name : ""}
          />
          <InputField
            label="Display Name"
            onChange={this.handleDisplayNameChange}
            value={user !== undefined ? user.displayName : ""}
          />
          <InputField
            label="E-Mail"
            onChange={this.handleEmailChange}
            value={user !== undefined ? user.mail : ""}
          />
          <InputField
            label="Password"
            type="password"
            onChange={this.handlePasswordChange}
            value={user !== undefined ? user.password : ""}
          />
          <Checkbox
            label="Admin"
            onChange={this.handleAdminChange}
            checked={user !== undefined ? user.admin : false}
          />
          <Checkbox
            label="Active"
            onChange={this.handleActiveChange}
            value={user !== undefined ? user.active : false}
          />
          <SubmitButton value="Submit" />
        </form>
      </div>
    );
  }

  handleUsernameChange = (name: string) => {
    this.setState({ name });
  };

  handleDisplayNameChange = (displayName: string) => {
    this.setState({ displayName });
  };

  handleEmailChange = (mail: string) => {
    this.setState({ mail });
  };

  handlePasswordChange = (password: string) => {
    this.setState({ password });
  };

  handleAdminChange = (admin: boolean) => {
    this.setState({ admin });
  };

  handleActiveChange = (active: boolean) => {
    this.setState({ active });
  };
}

export default UserForm;

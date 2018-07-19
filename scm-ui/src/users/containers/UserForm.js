// @flow
import React from "react";
import type { User } from "../types/User";
import InputField from "../../components/InputField";
import Checkbox from "../../components/Checkbox";
import SubmitButton from "../../components/SubmitButton";
import { connect } from "react-redux";

type Props = {
  submitForm: User => void,
  user?: User,
  loading?: boolean
};

class UserForm extends React.Component<Props, User> {
  constructor(props: Props) {
    super(props);
    this.state = {
      name: "",
      displayName: "",
      mail: "",
      password: "",
      admin: false,
      active: false
    };
  }

  componentDidMount() {
    this.setState({ ...this.props.user });
  }

  submit = (event: Event) => {
    event.preventDefault();
    this.props.submitForm(this.state);
  };

  render() {
    const user = this.state;
    if (user) {
      return (
        <div className="container">
          <form onSubmit={this.submit}>
            <InputField
              label="Username"
              onChange={this.handleUsernameChange}
              value={user ? user.name : ""}
            />
            <InputField
              label="Display Name"
              onChange={this.handleDisplayNameChange}
              value={user ? user.displayName : ""}
            />
            <InputField
              label="E-Mail"
              onChange={this.handleEmailChange}
              value={user ? user.mail : ""}
            />
            <InputField
              label="Password"
              type="password"
              onChange={this.handlePasswordChange}
              value={user ? user.password : ""}
            />
            <Checkbox
              label="Admin"
              onChange={this.handleAdminChange}
              checked={user ? user.admin : false}
            />
            <Checkbox
              label="Active"
              onChange={this.handleActiveChange}
              checked={user ? user.active : false}
            />
            <SubmitButton label="Submit" loading={this.props.loading} />
          </form>
        </div>
      );
    } else {
      return <div>Loading...</div>;
    }
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

// @flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "../types/User";
import InputField from "../../components/InputField";
import Checkbox from "../../components/Checkbox";
import SubmitButton from "../../components/SubmitButton";
import Loading from "../../components/Loading";

type Props = {
  submitForm: User => void,
  user?: User,
  loading?: boolean,
  t: string => string
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
      active: false,
      _links: {}
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
    const { t } = this.props;
    const user = this.state;
    if (user) {
      return (
        <div className="container">
          <form onSubmit={this.submit}>
            <InputField
              label={t("user.name")}
              onChange={this.handleUsernameChange}
              value={user ? user.name : ""}
            />
            <InputField
              label={t("user.displayName")}
              onChange={this.handleDisplayNameChange}
              value={user ? user.displayName : ""}
            />
            <InputField
              label={t("user.mail")}
              onChange={this.handleEmailChange}
              value={user ? user.mail : ""}
            />
            <InputField
              label={t("user.password")}
              type="password"
              onChange={this.handlePasswordChange}
              value={user ? user.password : ""}
            />
            <Checkbox
              label={t("user.admin")}
              onChange={this.handleAdminChange}
              checked={user ? user.admin : false}
            />
            <Checkbox
              label={t("user.active")}
              onChange={this.handleActiveChange}
              checked={user ? user.active : false}
            />
            <SubmitButton
              label={t("user-form.submit")}
              loading={this.props.loading}
            />
          </form>
        </div>
      );
    } else {
      return <Loading />;
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

export default translate("users")(UserForm);

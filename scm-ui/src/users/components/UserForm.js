// @flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "../types/User";
import { Checkbox, InputField } from "../../components/forms";
import { SubmitButton } from "../../components/buttons";
import * as validator from "./userValidation";

type Props = {
  submitForm: User => void,
  user?: User,
  t: string => string
};

type State = {
  user: User,
  mailValidationError: boolean,
  nameValidationError: boolean,
  displayNameValidationError: boolean,
  passwordValidationError: boolean,
  validatePasswordError: boolean,
  validatePassword: string
};

class UserForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      user: {
        name: "",
        displayName: "",
        mail: "",
        password: "",
        admin: false,
        active: false,
        _links: {}
      },
      mailValidationError: false,
      displayNameValidationError: false,
      nameValidationError: false,
      passwordValidationError: false,
      validatePasswordError: false,
      validatePassword: ""
    };
  }

  componentDidMount() {
    const { user } = this.props;
    if (user) {
      this.setState({ user: { ...user } });
    }
  }

  isFalsy(value) {
    if (!value) {
      return true;
    }
    return false;
  }

  isValid = () => {
    const user = this.state.user;
    return !(
      this.state.validatePasswordError ||
      this.state.nameValidationError ||
      this.state.mailValidationError ||
      this.state.validatePasswordError ||
      this.state.displayNameValidationError ||
      this.isFalsy(user.name) ||
      this.isFalsy(user.displayName)
    );
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.submitForm(this.state.user);
    }
  };

  render() {
    const { t } = this.props;
    const user = this.state.user;

    let nameField = null;
    if (!this.props.user) {
      nameField = (
        <InputField
          label={t("user.name")}
          onChange={this.handleUsernameChange}
          value={user ? user.name : ""}
          validationError={this.state.nameValidationError}
          errorMessage={t("validation.name-invalid")}
        />
      );
    }
    return (
      <form onSubmit={this.submit}>
        {nameField}
        <InputField
          label={t("user.displayName")}
          onChange={this.handleDisplayNameChange}
          value={user ? user.displayName : ""}
          validationError={this.state.displayNameValidationError}
          errorMessage={t("validation.displayname-invalid")}
        />
        <InputField
          label={t("user.mail")}
          onChange={this.handleEmailChange}
          value={user ? user.mail : ""}
          validationError={this.state.mailValidationError}
          errorMessage={t("validation.mail-invalid")}
        />
        <InputField
          label={t("user.password")}
          type="password"
          onChange={this.handlePasswordChange}
          value={user ? user.password : ""}
          validationError={this.state.validatePasswordError}
          errorMessage={t("validation.password-invalid")}
        />
        <InputField
          label={t("validation.validatePassword")}
          type="password"
          onChange={this.handlePasswordValidationChange}
          value={this.state ? this.state.validatePassword : ""}
          validationError={this.state.passwordValidationError}
          errorMessage={t("validation.passwordValidation-invalid")}
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
          disabled={!this.isValid()}
          label={t("user-form.submit")}
        />
      </form>
    );
  }

  handleUsernameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isNameValid(name),
      user: { ...this.state.user, name }
    });
  };

  handleDisplayNameChange = (displayName: string) => {
    this.setState({
      displayNameValidationError: !validator.isDisplayNameValid(displayName),
      user: { ...this.state.user, displayName }
    });
  };

  handleEmailChange = (mail: string) => {
    this.setState({
      mailValidationError: !validator.isMailValid(mail),
      user: { ...this.state.user, mail }
    });
  };

  handlePasswordChange = (password: string) => {
    const validatePasswordError = !this.checkPasswords(
      password,
      this.state.validatePassword
    );
    this.setState({
      validatePasswordError: !validator.isPasswordValid(password),
      passwordValidationError: validatePasswordError,
      user: { ...this.state.user, password }
    });
  };

  handlePasswordValidationChange = (validatePassword: string) => {
    const validatePasswordError = this.checkPasswords(
      this.state.user.password,
      validatePassword
    );
    this.setState({
      validatePassword,
      passwordValidationError: !validatePasswordError
    });
  };

  checkPasswords = (password1: string, password2: string) => {
    return password1 === password2;
  };

  handleAdminChange = (admin: boolean) => {
    this.setState({ user: { ...this.state.user, admin } });
  };

  handleActiveChange = (active: boolean) => {
    this.setState({ user: { ...this.state.user, active } });
  };
}

export default translate("users")(UserForm);

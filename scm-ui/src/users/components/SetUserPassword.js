// @flow
import React from "react";
import type { User } from "@scm-manager/ui-types";
import {
  InputField,
  SubmitButton,
  Notification
} from "@scm-manager/ui-components";
import * as userValidator from "./userValidation";
import { translate } from "react-i18next";
import { updatePassword } from "./updatePassword";

type Props = {
  user: User,
  t: string => string
};

type State = {
  password: string,
  loading: boolean,
  passwordValidationError: boolean,
  validatePasswordError: boolean,
  validatePassword: string,
  error?: Error,
  passwordChanged: boolean
};

class SetUserPassword extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      password: "",
      loading: false,
      passwordValidationError: false,
      validatePasswordError: false,
      validatePassword: "",
      passwordChanged: false
    };
  }

  isValid = () => {
    return !(
      this.state.validatePasswordError || this.state.passwordValidationError
    );
  };

  submit = (event: Event) => {
    //TODO: set loading
    event.preventDefault();
    if (this.isValid()) {
      const { user } = this.props;
      const { password } = this.state;
      updatePassword(user._links.password.href, password)
        .then(result => {
          if (result.error || result.status !== 204) {
            this.setState({
              ...this.state,
              error: result.error,
              loading: false
            });
          } else {
            this.setState({
              ...this.state,
              loading: false,
              passwordChanged: true,
              password: "",
              validatePassword: ""
            });
          }
        })
        .catch(err => {});
    }
  };

  render() {
    const { user, t } = this.props;
    const { loading, passwordChanged } = this.state;

    let passwordChangedSuccessful = null;

    if (passwordChanged) {
      passwordChangedSuccessful = (
        <Notification
          type={"success"}
          children={t("password.set-password-successful")}
          onClose={() => this.onClose()}
        />
      );
    }

    return (
      <form onSubmit={this.submit}>
        {passwordChangedSuccessful}
        <InputField
          label={t("user.password")}
          type="password"
          onChange={this.handlePasswordChange}
          value={this.state.password ? this.state.password : ""}
          validationError={this.state.validatePasswordError}
          errorMessage={t("validation.password-invalid")}
          helpText={t("help.passwordHelpText")}
        />
        <InputField
          label={t("validation.validatePassword")}
          type="password"
          onChange={this.handlePasswordValidationChange}
          value={this.state ? this.state.validatePassword : ""}
          validationError={this.state.passwordValidationError}
          errorMessage={t("validation.passwordValidation-invalid")}
          helpText={t("help.passwordConfirmHelpText")}
        />
        <SubmitButton
          disabled={!this.isValid()}
          loading={loading}
          label={t("user-form.submit")}
        />
      </form>
    );
  }

  handlePasswordChange = (password: string) => {
    const validatePasswordError = !this.checkPasswords(
      password,
      this.state.validatePassword
    );
    this.setState({
      validatePasswordError: !userValidator.isPasswordValid(password),
      passwordValidationError: validatePasswordError,
      password: password
    });
  };

  handlePasswordValidationChange = (validatePassword: string) => {
    const validatePasswordError = this.checkPasswords(
      this.state.password,
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

  onClose = () => {
    this.setState({
      ...this.state,
      passwordChanged: false
    });
  };
}

export default translate("users")(SetUserPassword);

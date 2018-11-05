// @flow
import React from "react";
import type { User } from "@scm-manager/ui-types";
import { InputField, SubmitButton } from "@scm-manager/ui-components";
import * as userValidator from "./userValidation";
import { translate } from "react-i18next";

type Props = {
  user: User,
  t: string => string
};

type State = {
  password: string,
  loading: boolean,
  passwordValidationError: boolean,
  validatePasswordError: boolean,
  validatePassword: string
};

class SetUserPassword extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      password: "",
      loading: false,
      passwordValidationError: false,
      validatePasswordError: false,
      validatePassword: ""
    };
  }

  isValid = () => {
    return !(
      this.state.validatePasswordError || this.state.passwordValidationError
    );
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      //TODO:hier update pw!
    }
  };

  render() {
    const { user, t } = this.props;
    const { loading } = this.state;
    return (
      <form onSubmit={this.submit}>
        <InputField
          label={t("user.password")}
          type="password"
          onChange={this.handlePasswordChange}
          value={user ? user.password : ""}
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
}

export default translate("users")(SetUserPassword);

// @flow

import React from "react";
import { InputField } from "@scm-manager/ui-components";
import { compose } from "redux";
import { translate } from "react-i18next";
import * as userValidator from "./userValidation";

type State = {
  password: string,
  confirmedPassword: string,
  passwordValid: boolean,
  passwordConfirmationFailed: boolean
};
type Props = {
  passwordChanged: string => void,
  // Context props
  t: string => string
};

class PasswordConfirmation extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      password: "",
      confirmedPassword: "",
      passwordValid: true,
      passwordConfirmationFailed: false
    };
  }

  componentDidMount() {
    this.setState({
      password: "",
      confirmedPassword: "",
      passwordValid: true,
      passwordConfirmationFailed: false
    });
  }

  render() {
    const { t } = this.props;
    return (
      <>
        <InputField
          label={t("user.password")}
          type="password"
          onChange={this.handlePasswordChange}
          value={this.state.password ? this.state.password : ""}
          validationError={!this.state.passwordValid}
          errorMessage={t("validation.password-invalid")}
          helpText={t("help.passwordHelpText")}
        />
        <InputField
          label={t("validation.validatePassword")}
          type="password"
          onChange={this.handlePasswordValidationChange}
          value={this.state ? this.state.confirmedPassword : ""}
          validationError={this.state.passwordConfirmationFailed}
          errorMessage={t("validation.passwordValidation-invalid")}
          helpText={t("help.passwordConfirmHelpText")}
        />
      </>
    );
  }

  handlePasswordValidationChange = (confirmedPassword: string) => {
    const passwordConfirmed = this.state.password === confirmedPassword;

    this.setState(
      {
        confirmedPassword,
        passwordConfirmationFailed: !passwordConfirmed
      },
      this.propagateChange
    );
  };

  handlePasswordChange = (password: string) => {
    const passwordConfirmationFailed =
      password !== this.state.confirmedPassword;

    this.setState(
      {
        passwordValid: userValidator.isPasswordValid(password),
        passwordConfirmationFailed,
        password: password
      },
      this.propagateChange
    );
  };

  propagateChange = () => {
    if (
      this.state.password &&
      this.state.passwordValid &&
      !this.state.passwordConfirmationFailed
    ) {
      this.props.passwordChanged(this.state.password);
    }
  };
}

export default compose(translate("users"))(PasswordConfirmation);

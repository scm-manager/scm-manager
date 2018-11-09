// @flow

import React from "react";
import {translate} from "react-i18next";
import InputField from "./InputField";

type State = {
  password: string,
  confirmedPassword: string,
  passwordValid: boolean,
  passwordConfirmationFailed: boolean
};
type Props = {
  passwordChanged: string => void,
  passwordValidator?: string => boolean,
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
          label={t("password.newPassword")}
          type="password"
          onChange={this.handlePasswordChange}
          value={this.state.password ? this.state.password : ""}
          validationError={!this.state.passwordValid}
          errorMessage={t("password.passwordInvalid")}
          helpText={t("password.passwordHelpText")}
        />
        <InputField
          label={t("password.confirmPassword")}
          type="password"
          onChange={this.handlePasswordValidationChange}
          value={this.state ? this.state.confirmedPassword : ""}
          validationError={this.state.passwordConfirmationFailed}
          errorMessage={t("password.passwordConfirmFailed")}
          helpText={t("password.passwordConfirmHelpText")}
        />
      </>
    );
  }

  validatePassword = password => {
    const { passwordValidator } = this.props;
    if (passwordValidator) {
      return passwordValidator(password);
    }

    return password.length >= 6 && password.length < 32;
  };

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
        passwordValid: this.validatePassword(password),
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

export default translate("commons")(PasswordConfirmation);

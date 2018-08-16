// @flow
import React from "react";
import { translate } from "react-i18next";
import { InputField } from "../../../components/forms/index";
import Subtitle from "../../../components/layout/Subtitle";
import * as validator from "../../../components/validation";

type Props = {
  loginAttemptLimit: number,
  loginAttemptLimitTimeout: number,
  t: string => string,
  onChange: (boolean, any, string) => void,
  hasUpdatePermission: boolean
};

type State = {
  loginAttemptLimitError: boolean,
  loginAttemptLimitTimeoutError: boolean
};

class LoginAttempt extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      loginAttemptLimitError: false,
      loginAttemptLimitTimeoutError: false
    };
  }
  render() {
    const {
      t,
      loginAttemptLimit,
      loginAttemptLimitTimeout,
      hasUpdatePermission
    } = this.props;

    return (
      <div>
        <Subtitle subtitle={t("login-attempt.name")} />
        <InputField
          label={t("login-attempt.login-attempt-limit")}
          onChange={this.handleLoginAttemptLimitChange}
          value={loginAttemptLimit}
          disabled={!hasUpdatePermission}
          validationError={this.state.loginAttemptLimitError}
          errorMessage={t("validation.login-attempt-limit-invalid")}
        />
        <InputField
          label={t("login-attempt.login-attempt-limit-timeout")}
          onChange={this.handleLoginAttemptLimitTimeoutChange}
          value={loginAttemptLimitTimeout}
          disabled={!hasUpdatePermission}
          validationError={this.state.loginAttemptLimitTimeoutError}
          errorMessage={t("validation.login-attempt-limit-timeout-invalid")}
        />
      </div>
    );
  }

  //TODO: set Error in ConfigForm to disable Submit Button!
  handleLoginAttemptLimitChange = (value: string) => {
    this.setState({
      ...this.state,
      loginAttemptLimitError: !validator.isNumberValid(value)
    });
    this.props.onChange(this.loginAttemptIsValid(), value, "loginAttemptLimit");
  };

  handleLoginAttemptLimitTimeoutChange = (value: string) => {
    this.setState({
      ...this.state,
      loginAttemptLimitTimeoutError: !validator.isNumberValid(value)
    });
    this.props.onChange(
      this.loginAttemptIsValid(),
      value,
      "loginAttemptLimitTimeout"
    );
  };

  loginAttemptIsValid = () => {
    return (
      this.state.loginAttemptLimitError ||
      this.state.loginAttemptLimitTimeoutError
    );
  };
}

export default translate("config")(LoginAttempt);

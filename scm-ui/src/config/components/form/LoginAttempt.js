// @flow
import React from "react";
import { translate } from "react-i18next";
import {
  InputField,
  Subtitle,
  validation as validator
} from "@scm-manager/ui-components";

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
        <div class="columns">
                <div class="column is-half">
                    <InputField
                      label={t("login-attempt.login-attempt-limit")}
                      onChange={this.handleLoginAttemptLimitChange}
                      value={loginAttemptLimit}
                      disabled={!hasUpdatePermission}
                      validationError={this.state.loginAttemptLimitError}
                      errorMessage={t("validation.login-attempt-limit-invalid")}
                      helpText={t("help.loginAttemptLimitHelpText")}
                    />
                </div>
                <div class="column is-half">
                    <InputField
                      label={t("login-attempt.login-attempt-limit-timeout")}
                      onChange={this.handleLoginAttemptLimitTimeoutChange}
                      value={loginAttemptLimitTimeout}
                      disabled={!hasUpdatePermission}
                      validationError={this.state.loginAttemptLimitTimeoutError}
                      errorMessage={t("validation.login-attempt-limit-timeout-invalid")}
                      helpText={t("help.loginAttemptLimitTimeoutHelpText")}
                    />
              </div>
            </div>
      </div>
    );
  }

  //TODO: set Error in ConfigForm to disable Submit Button!
  handleLoginAttemptLimitChange = (value: string) => {
    this.setState({
      ...this.state,
      loginAttemptLimitError: !validator.isNumberValid(value)
    });
    this.props.onChange(
      validator.isNumberValid(value),
      value,
      "loginAttemptLimit"
    );
  };

  handleLoginAttemptLimitTimeoutChange = (value: string) => {
    this.setState({
      ...this.state,
      loginAttemptLimitTimeoutError: !validator.isNumberValid(value)
    });
    this.props.onChange(
      validator.isNumberValid(value),
      value,
      "loginAttemptLimitTimeout"
    );
  };
}

export default translate("config")(LoginAttempt);

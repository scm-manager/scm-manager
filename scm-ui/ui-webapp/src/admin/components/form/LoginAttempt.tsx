/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { InputField, Subtitle, validation as validator } from "@scm-manager/ui-components";
import { ConfigChangeHandler } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  loginAttemptLimit: number;
  loginAttemptLimitTimeout: number;
  onChange: ConfigChangeHandler;
  hasUpdatePermission: boolean;
};

type State = {
  loginAttemptLimitError: boolean;
  loginAttemptLimitTimeoutError: boolean;
};

class LoginAttempt extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      loginAttemptLimitError: false,
      loginAttemptLimitTimeoutError: false,
    };
  }
  render() {
    const { t, loginAttemptLimit, loginAttemptLimitTimeout, hasUpdatePermission } = this.props;

    return (
      <div>
        <Subtitle subtitle={t("login-attempt.name")} />
        <div className="columns">
          <div className="column">
            <InputField
              type="number"
              label={t("login-attempt.login-attempt-limit")}
              onChange={this.handleLoginAttemptLimitChange}
              value={loginAttemptLimit}
              disabled={!hasUpdatePermission}
              validationError={this.state.loginAttemptLimitError}
              errorMessage={t("validation.login-attempt-limit-invalid")}
              helpText={t("help.loginAttemptLimitHelpText")}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column">
            <InputField
              type="number"
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
      loginAttemptLimitError: !validator.isNumberValid(value),
    });
    this.props.onChange(validator.isNumberValid(value), Number(value), "loginAttemptLimit");
  };

  handleLoginAttemptLimitTimeoutChange = (value: string) => {
    this.setState({
      ...this.state,
      loginAttemptLimitTimeoutError: !validator.isNumberValid(value),
    });
    this.props.onChange(validator.isNumberValid(value), Number(value), "loginAttemptLimitTimeout");
  };
}

export default withTranslation("config")(LoginAttempt);

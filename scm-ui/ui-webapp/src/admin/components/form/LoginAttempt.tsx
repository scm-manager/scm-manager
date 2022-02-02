/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
      loginAttemptLimitTimeoutError: false
    };
  }
  render() {
    const { t, loginAttemptLimit, loginAttemptLimitTimeout, hasUpdatePermission } = this.props;

    return (
      <div>
        <Subtitle subtitle={t("login-attempt.name")} />
        <div className="columns">
          <div className="column is-half">
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
          <div className="column is-half">
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
      loginAttemptLimitError: !validator.isNumberValid(value)
    });
    this.props.onChange(validator.isNumberValid(value), Number(value), "loginAttemptLimit");
  };

  handleLoginAttemptLimitTimeoutChange = (value: string) => {
    this.setState({
      ...this.state,
      loginAttemptLimitTimeoutError: !validator.isNumberValid(value)
    });
    this.props.onChange(validator.isNumberValid(value), Number(value), "loginAttemptLimitTimeout");
  };
}

export default withTranslation("config")(LoginAttempt);

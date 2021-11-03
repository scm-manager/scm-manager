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
import InputField from "./InputField";

type State = {
  password: string;
  confirmedPassword: string;
  passwordValid: boolean;
  passwordConfirmationFailed: boolean;
};
type Props = WithTranslation & {
  passwordChanged: (p1: string, p2: boolean) => void;
  passwordValidator?: (p: string) => boolean;
  onReturnPressed?: () => void;
};

class PasswordConfirmation extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      password: "",
      confirmedPassword: "",
      passwordValid: true,
      passwordConfirmationFailed: false,
    };
  }

  componentDidMount() {
    this.setState({
      password: "",
      confirmedPassword: "",
      passwordValid: true,
      passwordConfirmationFailed: false,
    });
  }

  render() {
    const { t, onReturnPressed } = this.props;
    return (
      <div className="columns is-multiline">
        <div className="column is-half">
          <InputField
            label={t("password.newPassword")}
            type="password"
            onChange={this.handlePasswordChange}
            value={this.state.password ? this.state.password : ""}
            validationError={!this.state.passwordValid}
            errorMessage={t("password.passwordInvalid")}
          />
        </div>
        <div className="column is-half">
          <InputField
            label={t("password.confirmPassword")}
            type="password"
            onChange={this.handlePasswordValidationChange}
            value={this.state ? this.state.confirmedPassword : ""}
            validationError={this.state.passwordConfirmationFailed}
            errorMessage={t("password.passwordConfirmFailed")}
            onReturnPressed={onReturnPressed}
          />
        </div>
      </div>
    );
  }

  validatePassword = (password: string) => {
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
        passwordConfirmationFailed: !passwordConfirmed,
      },
      this.propagateChange
    );
  };

  handlePasswordChange = (password: string) => {
    const passwordConfirmationFailed = password !== this.state.confirmedPassword;

    this.setState(
      {
        passwordValid: this.validatePassword(password),
        passwordConfirmationFailed,
        password: password,
      },
      this.propagateChange
    );
  };

  isValid = () => {
    return this.state.passwordValid && !this.state.passwordConfirmationFailed;
  };

  propagateChange = () => {
    this.props.passwordChanged(this.state.password, this.isValid());
  };
}

export default withTranslation("commons")(PasswordConfirmation);

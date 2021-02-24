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
import {
  ErrorNotification,
  InputField,
  Level,
  Notification,
  PasswordConfirmation,
  SubmitButton,
  Subtitle
} from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";
import { Me } from "@scm-manager/ui-types";
import { changePassword } from "../utils/changePassword";

type Props = WithTranslation & {
  me: Me;
};

type State = {
  oldPassword: string;
  password: string;
  loading: boolean;
  error?: Error;
  passwordChanged: boolean;
  passwordValid: boolean;
};

class ChangeUserPassword extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      oldPassword: "",
      password: "",
      loading: false,
      passwordChanged: false,
      passwordValid: false
    };
  }

  setLoadingState = () => {
    this.setState({
      ...this.state,
      loading: true
    });
  };

  setErrorState = (error: Error) => {
    this.setState({
      ...this.state,
      error: error,
      loading: false
    });
  };

  setSuccessfulState = () => {
    this.setState({
      ...this.state,
      loading: false,
      passwordChanged: true,
      oldPassword: "",
      password: ""
    });
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.state.password) {
      const { oldPassword, password } = this.state;
      this.setLoadingState();
      changePassword(this.props.me._links.password.href, oldPassword, password)
        .then(result => {
          if (result.error) {
            this.setErrorState(result.error);
          } else {
            this.setSuccessfulState();
          }
        })
        .catch(err => {
          this.setErrorState(err);
        });
    }
  };

  isValid = () => {
    return this.state.oldPassword && this.state.passwordValid;
  };

  render() {
    const { t } = this.props;
    const { loading, passwordChanged, error } = this.state;

    let message = null;

    if (passwordChanged) {
      message = (
        <Notification type={"success"} children={t("password.changedSuccessfully")} onClose={() => this.onClose()} />
      );
    } else if (error) {
      message = <ErrorNotification error={error} />;
    }

    return (
      <form onSubmit={this.submit}>
        <Subtitle subtitle={t("password.subtitle")} />
        {message}
        <div className="columns">
          <div className="column">
            <InputField
              label={t("password.currentPassword")}
              type="password"
              onChange={oldPassword =>
                this.setState({
                  ...this.state,
                  oldPassword
                })
              }
              value={this.state.oldPassword ? this.state.oldPassword : ""}
              helpText={t("password.currentPasswordHelpText")}
            />
          </div>
        </div>
        <PasswordConfirmation
          passwordChanged={this.passwordChanged}
          key={this.state.passwordChanged ? "changed" : "unchanged"}
        />
        <Level right={<SubmitButton disabled={!this.isValid()} loading={loading} label={t("password.submit")} />} />
      </form>
    );
  }

  passwordChanged = (password: string, passwordValid: boolean) => {
    this.setState({
      ...this.state,
      password,
      passwordValid: !!password && passwordValid
    });
  };

  onClose = () => {
    this.setState({
      ...this.state,
      passwordChanged: false
    });
  };
}

export default withTranslation("commons")(ChangeUserPassword);

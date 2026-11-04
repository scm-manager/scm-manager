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

import React, { FormEvent } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Button, ErrorNotification, Field, Input } from "@scm-manager/ui-core";
import { UnauthorizedError } from "@scm-manager/ui-api";

type Props = WithTranslation & {
  error?: Error | null;
  loading?: boolean;
  loginHandler: (username: string, password: string) => void;
};

type State = {
  username: string;
  password: string;
};

class LoginForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      username: "",
      password: "",
    };
  }

  handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.loginHandler(this.state.username, this.state.password);
    }
  };

  handleUsernameChange = (value: string) => {
    this.setState({
      username: value,
    });
  };

  handlePasswordChange = (value: string) => {
    this.setState({
      password: value,
    });
  };

  isValid() {
    return this.state.username && this.state.password;
  }

  areCredentialsInvalid() {
    const { t, error } = this.props;
    if (error && error instanceof UnauthorizedError) {
      return new Error(t("errorNotification.wrongLoginCredentials"));
    } else {
      return error;
    }
  }

  render() {
    const { loading, t } = this.props;
    return (
      <>
        <ErrorNotification error={this.areCredentialsInvalid()} />
        <form onSubmit={this.handleSubmit}>
          <Field>
            <div className="control">
              <Input
                testId="username-input"
                placeholder={t("login.username-placeholder")}
                aria-label={t("login.username-placeholder")}
                autoFocus={true}
                onChange={(event) => this.handleUsernameChange(event.target.value)}
                disabled={loading}
              />
            </div>
          </Field>
          <Field>
            <div className="control">
              <Input
                testId="password-input"
                placeholder={t("login.password-placeholder")}
                aria-label={t("login.password-placeholder")}
                type="password"
                onChange={(event) => this.handlePasswordChange(event.target.value)}
                disabled={loading}
              />
            </div>
          </Field>
          <Button
            type="submit"
            variant="primary"
            className="is-fullwidth"
            isLoading={loading}
            disabled={loading}
            testId="login-button"
          >
            {t("login.submit")}
          </Button>
        </form>
      </>
    );
  }
}

export default withTranslation("commons")(LoginForm);

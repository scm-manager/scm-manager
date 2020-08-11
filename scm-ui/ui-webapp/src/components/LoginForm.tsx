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
import styled from "styled-components";
import { ErrorNotification, Image, InputField, SubmitButton, UnauthorizedError } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  error?: Error;
  loading: boolean;
  loginHandler: (username: string, password: string) => void;
};

type State = {
  username: string;
  password: string;
};

const TopMarginBox = styled.div`
  margin-top: 5rem;
`;

const AvatarWrapper = styled.figure`
  margin-top: -70px;
  padding-bottom: 20px;
`;

const AvatarImage = styled(Image)`
  width: 128px;
  height: 128px;
  padding: 5px;
  background: #fff;
  border: 1px solid lightgray;
  border-radius: 50%;
`;

class LoginForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      username: "",
      password: ""
    };
  }

  handleSubmit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.loginHandler(this.state.username, this.state.password);
    }
  };

  handleUsernameChange = (value: string) => {
    this.setState({
      username: value
    });
  };

  handlePasswordChange = (value: string) => {
    this.setState({
      password: value
    });
  };

  isValid() {
    return this.state.username && this.state.password;
  }

  areCredentialsInvalid() {
    const { t, error } = this.props;
    if (error instanceof UnauthorizedError) {
      return new Error(t("errorNotification.wrongLoginCredentials"));
    } else {
      return error;
    }
  }

  render() {
    const { loading, t } = this.props;
    return (
      <div className="column is-4 box has-text-centered has-background-white-ter">
        <h3 className="title">{t("login.title")}</h3>
        <p className="subtitle">{t("login.subtitle")}</p>
        <TopMarginBox className="box">
          <AvatarWrapper>
            <AvatarImage src="/images/blib.jpg" alt={t("login.logo-alt")} />
          </AvatarWrapper>
          <ErrorNotification error={this.areCredentialsInvalid()} />
          <form onSubmit={this.handleSubmit}>
            <InputField
              testId="username-input"
              placeholder={t("login.username-placeholder")}
              autofocus={true}
              onChange={this.handleUsernameChange}
            />
            <InputField
              testId="password-input"
              placeholder={t("login.password-placeholder")}
              type="password"
              onChange={this.handlePasswordChange}
            />
            <SubmitButton label={t("login.submit")} fullWidth={true} loading={loading} testId="login-button" />
          </form>
        </TopMarginBox>
      </div>
    );
  }
}

export default withTranslation("commons")(LoginForm);

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
import { connect } from "react-redux";
import { Redirect, RouteComponentProps, withRouter } from "react-router-dom";
import { compose } from "redux";
import styled from "styled-components";
import { getLoginFailure, getMe, isAnonymous, isLoginPending, login } from "../modules/auth";
import { getLoginInfoLink, getLoginLink } from "../modules/indexResource";
import LoginInfo from "../components/LoginInfo";
import { Me } from "@scm-manager/ui-types";

type Props = RouteComponentProps & {
  authenticated: boolean;
  me: Me;
  loading: boolean;
  error?: Error;
  link: string;
  loginInfoLink?: string;

  // dispatcher props
  login: (link: string, username: string, password: string) => void;
};

const HeroSection = styled.section`
  padding-top: 2em;
`;

class Login extends React.Component<Props> {
  handleLogin = (username: string, password: string): void => {
    const { link, login } = this.props;
    login(link, username, password);
  };

  renderRedirect = () => {
    const { from } = this.props.location.state || {
      from: {
        pathname: "/"
      }
    };
    return <Redirect to={from} />;
  };

  render() {
    const { authenticated, me, ...restProps } = this.props;

    if (authenticated && !!me) {
      return this.renderRedirect();
    }

    return (
      <HeroSection className="hero">
        <div className="hero-body">
          <div className="container">
            <div className="columns is-centered">
              <LoginInfo loginHandler={this.handleLogin} {...restProps} />
            </div>
          </div>
        </div>
      </HeroSection>
    );
  }
}

const mapStateToProps = (state: any) => {
  const authenticated = state?.auth?.me && !isAnonymous(state.auth.me);
  const me = getMe(state);
  const loading = isLoginPending(state);
  const error = getLoginFailure(state);
  const link = getLoginLink(state);
  const loginInfoLink = getLoginInfoLink(state);
  return {
    authenticated,
    me,
    loading,
    error,
    link,
    loginInfoLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    login: (loginLink: string, username: string, password: string) => dispatch(login(loginLink, username, password))
  };
};

export default compose(withRouter, connect(mapStateToProps, mapDispatchToProps))(Login);

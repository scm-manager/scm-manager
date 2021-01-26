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
import React, { FC } from "react";
import { connect } from "react-redux";
import { Redirect, RouteComponentProps, useLocation, withRouter } from "react-router-dom";
import { compose } from "redux";
import styled from "styled-components";
import { getLoginFailure, getMe, isAnonymous, isLoginPending, login } from "../modules/auth";
import { getLoginInfoLink, getLoginLink } from "../modules/indexResource";
import LoginInfo from "../components/LoginInfo";
import { Me } from "@scm-manager/ui-types";
import { parse } from "query-string";
import { useIndexLink, useLogin, useSubject } from "@scm-manager/ui-api";

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

interface FromObject {
  from?: string;
}

/**
 * @visibleForTesting
 */
export const from = (queryString?: string, stateParams?: FromObject | null): string => {
  const queryParams = parse(queryString || "");
  return queryParams?.from || stateParams?.from || "/";
};

const Login: FC = ({}) => {
  const { isAuthenticated, me } = useSubject();
  const location = useLocation<FromObject>();
  const { login, isLoading, error } = useLogin();
  const loginInfoLink = useIndexLink("loginInfo");

  if (isAuthenticated && !!me) {
    const to = from(window.location.search, location.state);
    return <Redirect to={to} />;
  }

  return (
    <HeroSection className="hero">
      <div className="hero-body">
        <div className="container">
          <div className="columns is-centered">
            <LoginInfo loginHandler={login} loading={isLoading} error={error} loginInfoLink={loginInfoLink} />
          </div>
        </div>
      </div>
    </HeroSection>
  );
};

export default Login;

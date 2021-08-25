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
import { Redirect, useLocation } from "react-router-dom";
import LoginInfo from "../components/LoginInfo";
import { parse } from "query-string";
import { useIndexLink, useLogin } from "@scm-manager/ui-api";

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

const Login: FC = () => {
  const location = useLocation<FromObject>();
  const { login, isLoading, error } = useLogin();
  const loginInfoLink = useIndexLink("loginInfo");

  // sometimes after logout the url is still /logout
  // but it does not make sense to redirect to /logout
  // directly after login
  if (location.pathname === "/logout") {
    return <Redirect to="/" />;
  }

  if (!login) {
    const to = from(window.location.search, location.state);
    return <Redirect to={to} />;
  }

  return (
    <section className="hero pt-6">
      <div className="hero-body">
        <div className="container">
          <div className="columns is-centered">
            <LoginInfo loginHandler={login} loading={isLoading} error={error} loginInfoLink={loginInfoLink} />
          </div>
        </div>
      </div>
    </section>
  );
};

export default Login;

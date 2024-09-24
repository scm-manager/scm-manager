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

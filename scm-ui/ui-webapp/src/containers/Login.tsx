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
import { Navigate, useLoaderData, useLocation, useNavigate } from "react-router-dom";
import LoginInfo from "../components/LoginInfo";
import { parse } from "query-string";
import { useLogin } from "@scm-manager/ui-api";
import { IndexResources, Link } from "@scm-manager/ui-types";

interface FromObject {
  from?: string;
}

/**
 * @visibleForTesting
 */
export const from = (queryString?: string, stateParams?: FromObject | null): string => {
  const queryParams = parse(queryString || "");
  //@ts-ignore
  return queryParams?.from || stateParams?.from || "/";
};

const Login: FC = () => {
  const location = useLocation();
  const index = useLoaderData<IndexResources>();
  const navigate = useNavigate();
  const { login, isPending, error } = useLogin((index._links.login as Link)?.href, () => {
    const to = from(window.location.search, location.state);
    return navigate(to);
  });
  const loginInfoLink = (index._links.loginInfo as Link)?.href;

  // sometimes after logout the url is still /logout
  // but it does not make sense to redirect to /logout
  // directly after login
  if (location.pathname === "/logout") {
    return <Navigate to="/" />;
  }

  if (!login) {
    const to = from(window.location.search, location.state);
    return <Navigate to={to} />;
  }

  return (
    <section className="hero pt-6">
      <div className="hero-body">
        <div className="container">
          <div className="columns is-centered">
            <LoginInfo loginHandler={login} loading={isPending} error={error} loginInfoLink={loginInfoLink} />
          </div>
        </div>
      </div>
    </section>
  );
};

export default Login;

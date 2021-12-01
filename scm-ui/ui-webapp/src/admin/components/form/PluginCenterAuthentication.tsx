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
import { usePluginCenterAuthInfo, usePluginCenterLogout } from "@scm-manager/ui-api";
import { Button, ErrorNotification, Notification} from "@scm-manager/ui-components";
import { Link, PluginCenterAuthenticationInfo } from "@scm-manager/ui-types";
import {useLocation} from "react-router-dom";

type AuthenticatedInfoProps = {
  authenticationInfo: PluginCenterAuthenticationInfo;
};

const AuthenticatedInfo: FC<AuthenticatedInfoProps> = ({ authenticationInfo }) => {
  const { logout, isLoading, error } = usePluginCenterLogout(authenticationInfo);

  return (
    <Notification type="inherit">
      <div className="is-full-width is-flex is-justify-content-space-between is-align-content-center">
        <p>Plugin Center is authenticated as {authenticationInfo.principal}</p>
        {authenticationInfo._links.logout ? (
          <Button color="warning" loading={isLoading} action={logout}>
            Logout
          </Button>
        ) : null}
      </div>
      {error ? (
        <div className="pt-4">
          <ErrorNotification error={error} />
        </div>
      ) : null}
    </Notification>
  );
};

const PluginCenterAuthentication: FC = () => {
  const { data, isLoading, error } = usePluginCenterAuthInfo();
  const location = useLocation();

  if (isLoading) {
    // TODO i18n
    return (
      <div className="is-flex is-align-content-center">
        <span className="small-loading-spinner pt-1 pr-3" />
        <p>Loading authentication info ...</p>
      </div>
    );
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!data) {
    return null;
  }

  if (data.principal) {
    return <AuthenticatedInfo authenticationInfo={data} />;
  }

  return (
    <Notification type="inherit" className="is-flex is-justify-content-space-between is-align-content-center">
      <p>Plugin Center is not authenticated</p>
      {data._links.login ? (
        <Button color="primary" link={(data._links.login as Link).href + "?source=" + location.pathname}>
          Authenticate
        </Button>
      ) : null}
    </Notification>
  );
};

export default PluginCenterAuthentication;

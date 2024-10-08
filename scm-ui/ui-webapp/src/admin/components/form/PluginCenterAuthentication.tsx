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
import { usePluginCenterAuthInfo, usePluginCenterLogout } from "@scm-manager/ui-api";
import { Button, ErrorNotification, Notification, Tooltip, useDateFormatter } from "@scm-manager/ui-components";
import { Link, PluginCenterAuthenticationInfo } from "@scm-manager/ui-types";
import styled from "styled-components";
import { Trans, useTranslation } from "react-i18next";

const Message = styled.p`
  line-height: 2.5rem;
`;

type Props = {
  authenticationInfo: PluginCenterAuthenticationInfo;
};

const PluginCenterSubject: FC<Props> = ({ authenticationInfo }) => {
  const formatter = useDateFormatter({ date: authenticationInfo.date });
  const [t] = useTranslation("config");
  return (
    <>
      <Tooltip
        location="top"
        message={t("pluginSettings.auth.subjectTooltip", {
          principal: authenticationInfo.principal,
          ago: formatter?.formatDistance()
        })}
      >
        <strong>{authenticationInfo.pluginCenterSubject}</strong>
      </Tooltip>
    </>
  );
};

const AuthenticatedInfo: FC<Props> = ({ authenticationInfo }) => {
  const { logout, isLoading, error } = usePluginCenterLogout(authenticationInfo);
  const [t] = useTranslation("config");

  const subject = <PluginCenterSubject authenticationInfo={authenticationInfo} />;

  return (
    <Notification type="inherit">
      <div className="is-full-width is-flex is-justify-content-space-between is-align-content-center">
        <Message>
          <Trans t={t} i18nKey="pluginSettings.auth.authenticated" components={[subject]} />
        </Message>
        {authenticationInfo._links.logout ? (
          <Button color="warning" loading={isLoading} action={logout}>
            {t("pluginSettings.auth.logout")}
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

const LoginButton: FC<{ link: Link }> = ({ link }) => {
  const [t] = useTranslation("config");
  return (
    <Button color="primary" link={link.href}>
      {t("pluginSettings.auth.authenticate")}
    </Button>
  );
};

const PluginCenterAuthentication: FC = () => {
  const { data, isLoading, error } = usePluginCenterAuthInfo();
  const [t] = useTranslation("config");

  if (isLoading) {
    return (
      <div className="is-flex is-align-content-center">
        <span className="small-loading-spinner pt-1 pr-3" />
        <p>{t("pluginSettings.auth.loading")}</p>
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

  if (data._links.login) {
    return (
      <Notification type="inherit" className="is-flex is-justify-content-space-between is-align-content-center">
        <Message>{t("pluginSettings.auth.notAuthenticated")}</Message>
        <LoginButton link={data._links.login as Link} />
      </Notification>
    );
  } else {
    return null;
  }
};

export default PluginCenterAuthentication;

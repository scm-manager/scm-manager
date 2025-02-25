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

import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Config, ConfigChangeHandler, Link, NamespaceStrategies } from "@scm-manager/ui-types";
import { Level, Notification, SubmitButton } from "@scm-manager/ui-components";
import ProxySettings from "./ProxySettings";
import GeneralSettings from "./GeneralSettings";
import BaseUrlSettings from "./BaseUrlSettings";
import LoginAttempt from "./LoginAttempt";
import PluginSettings from "./PluginSettings";
import FunctionSettings from "./FunctionSettings";
import InvalidateCaches from "./InvalidateCaches";
import InvalidateSearchIndex from "./InvalidateSearchIndex";
import JwtSettings from "./JwtSettings";

type Props = {
  submitForm: (p: Config) => void;
  config?: Config;
  loading?: boolean;
  configReadPermission: boolean;
  configUpdatePermission: boolean;
  namespaceStrategies?: NamespaceStrategies;
  invalidateCachesLink?: Link;
  invalidateSearchIndexLink?: Link;
};

const ConfigForm: FC<Props> = ({
  submitForm,
  config,
  loading,
  configReadPermission,
  configUpdatePermission,
  namespaceStrategies,
  invalidateCachesLink,
  invalidateSearchIndexLink,
}) => {
  const [t] = useTranslation("config");
  const [innerConfig, setInnerConfig] = useState<Config>({
    proxyPassword: null,
    proxyPort: 0,
    proxyServer: "",
    proxyUser: null,
    enableProxy: false,
    realmDescription: "",
    disableGroupingGrid: false,
    dateFormat: "",
    anonymousAccessEnabled: false,
    anonymousMode: "OFF",
    enabledFileSearch: true,
    baseUrl: "",
    forceBaseUrl: false,
    loginAttemptLimit: 0,
    proxyExcludes: [],
    skipFailedAuthenticators: false,
    pluginUrl: "",
    pluginAuthUrl: "",
    loginAttemptLimitTimeout: 0,
    enabledXsrfProtection: true,
    enabledUserConverter: false,
    namespaceStrategy: "",
    loginInfoUrl: "",
    alertsUrl: "",
    releaseFeedUrl: "",
    mailDomainName: "",
    emergencyContacts: [],
    enabledApiKeys: true,
    jwtExpirationInH: 1,
    enabledJwtEndless: false,
    _links: {},
  });
  const [showNotification, setShowNotification] = useState(false);
  const [changed, setChanged] = useState(false);
  const [error, setError] = useState<{
    loginAttemptLimitTimeout: boolean;
    loginAttemptLimit: boolean;
  }>({
    loginAttemptLimitTimeout: false,
    loginAttemptLimit: false,
  });

  useEffect(() => {
    if (config) {
      setInnerConfig(config);
    }
    if (!configUpdatePermission) {
      setShowNotification(true);
    }
  }, [config, configUpdatePermission]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setChanged(false);
    submitForm(innerConfig);
  };

  const onChange: ConfigChangeHandler = (isValid: boolean, changedValue: unknown, name: string) => {
    setInnerConfig({ ...innerConfig, [name]: changedValue });
    setError({ ...error, [name]: !isValid });
    setChanged(true);
  };

  const hasError = () => {
    return error.loginAttemptLimit || error.loginAttemptLimitTimeout;
  };

  const onClose = () => {
    setShowNotification(false);
  };

  let noPermissionNotification = null;

  if (!configReadPermission) {
    return <Notification type={"danger"} children={t("config.form.no-read-permission-notification")} />;
  }

  if (showNotification) {
    noPermissionNotification = (
      <Notification
        type={"info"}
        children={t("config.form.no-write-permission-notification")}
        onClose={() => onClose()}
      />
    );
  }

  return (
    <form onSubmit={submit}>
      {noPermissionNotification}
      <GeneralSettings
        namespaceStrategies={namespaceStrategies}
        loginInfoUrl={innerConfig.loginInfoUrl}
        realmDescription={innerConfig.realmDescription}
        disableGroupingGrid={innerConfig.disableGroupingGrid}
        dateFormat={innerConfig.dateFormat}
        anonymousMode={innerConfig.anonymousMode}
        skipFailedAuthenticators={innerConfig.skipFailedAuthenticators}
        alertsUrl={innerConfig.alertsUrl}
        releaseFeedUrl={innerConfig.releaseFeedUrl}
        mailDomainName={innerConfig.mailDomainName}
        enabledXsrfProtection={innerConfig.enabledXsrfProtection}
        emergencyContacts={innerConfig.emergencyContacts}
        namespaceStrategy={innerConfig.namespaceStrategy}
        onChange={onChange}
        hasUpdatePermission={configUpdatePermission}
      />
      <hr />
      <FunctionSettings
        enabledUserConverter={innerConfig.enabledUserConverter}
        enabledApiKeys={innerConfig.enabledApiKeys}
        enabledFileSearch={innerConfig.enabledFileSearch}
        onChange={onChange}
        hasUpdatePermission={configUpdatePermission}
      />
      <hr />
      <LoginAttempt
        loginAttemptLimit={innerConfig.loginAttemptLimit}
        loginAttemptLimitTimeout={innerConfig.loginAttemptLimitTimeout}
        onChange={onChange}
        hasUpdatePermission={configUpdatePermission}
      />
      <hr />
      <BaseUrlSettings
        baseUrl={innerConfig.baseUrl}
        forceBaseUrl={innerConfig.forceBaseUrl}
        onChange={onChange}
        hasUpdatePermission={configUpdatePermission}
      />
      <hr />
      <PluginSettings
        pluginUrl={innerConfig.pluginUrl}
        pluginAuthUrl={innerConfig.pluginAuthUrl}
        onChange={(isValid, changedValue, name) => onChange(isValid, changedValue, name)}
        hasUpdatePermission={configUpdatePermission}
      />
      <hr />
      <JwtSettings
        enabledJwtEndless={innerConfig.enabledJwtEndless || false}
        jwtExpirationInH={innerConfig.jwtExpirationInH || 1}
        onChange={onChange}
        hasUpdatePermission={configUpdatePermission}
      />
      <hr />
      <ProxySettings
        proxyPassword={innerConfig.proxyPassword ? innerConfig.proxyPassword : ""}
        proxyPort={innerConfig.proxyPort ? innerConfig.proxyPort : 0}
        proxyServer={innerConfig.proxyServer ? innerConfig.proxyServer : ""}
        proxyUser={innerConfig.proxyUser ? innerConfig.proxyUser : ""}
        enableProxy={innerConfig.enableProxy}
        proxyExcludes={innerConfig.proxyExcludes}
        onChange={onChange}
        hasUpdatePermission={configUpdatePermission}
      />
      <hr />
      {invalidateCachesLink ? (
        <>
          <InvalidateCaches />
          <hr />
        </>
      ) : null}
      {invalidateSearchIndexLink ? (
        <>
          <InvalidateSearchIndex />
          <hr />
        </>
      ) : null}
      <Level
        right={
          <SubmitButton
            loading={loading}
            label={t("config.form.submit")}
            disabled={!configUpdatePermission || hasError() || !changed}
          />
        }
      />
    </form>
  );
};

export default ConfigForm;

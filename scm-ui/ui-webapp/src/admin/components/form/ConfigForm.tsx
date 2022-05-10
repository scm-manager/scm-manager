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
import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Config, ConfigChangeHandler, NamespaceStrategies } from "@scm-manager/ui-types";
import { Level, Notification, SubmitButton } from "@scm-manager/ui-components";
import ProxySettings from "./ProxySettings";
import GeneralSettings from "./GeneralSettings";
import BaseUrlSettings from "./BaseUrlSettings";
import LoginAttempt from "./LoginAttempt";
import PluginSettings from "./PluginSettings";

type Props = {
  submitForm: (p: Config) => void;
  config?: Config;
  loading?: boolean;
  configReadPermission: boolean;
  configUpdatePermission: boolean;
  namespaceStrategies?: NamespaceStrategies;
};

const ConfigForm: FC<Props> = ({
  submitForm,
  config,
  loading,
  configReadPermission,
  configUpdatePermission,
  namespaceStrategies,
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
    feedbackUrl: "",
    releaseFeedUrl: "",
    mailDomainName: "",
    emergencyContacts: [],
    enabledApiKeys: true,
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
        enabledUserConverter={innerConfig.enabledUserConverter}
        enabledApiKeys={innerConfig.enabledApiKeys}
        emergencyContacts={innerConfig.emergencyContacts}
        namespaceStrategy={innerConfig.namespaceStrategy}
        feedbackUrl={innerConfig.feedbackUrl}
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

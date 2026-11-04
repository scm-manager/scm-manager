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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { useUserOptions } from "@scm-manager/ui-api";
import { Checkbox, ChipInputField, Combobox, Field, InputField, Label, SelectField } from "@scm-manager/ui-core";
import {
  AnonymousMode,
  AutocompleteObject,
  ConfigChangeHandler,
  NamespaceStrategies,
  Option,
} from "@scm-manager/ui-types";
import NamespaceStrategySelect from "./NamespaceStrategySelect";

type Props = {
  realmDescription: string;
  loginInfoUrl: string;
  pluginUrl: string;
  disableGroupingGrid: boolean;
  dateFormat: string;
  anonymousMode: AnonymousMode;
  skipFailedAuthenticators: boolean;
  alertsUrl: string;
  releaseFeedUrl: string;
  mailDomainName: string;
  enabledXsrfProtection: boolean;
  emergencyContacts: string[];
  namespaceStrategy: string;
  namespaceStrategies?: NamespaceStrategies;
  onChange: ConfigChangeHandler;
  hasUpdatePermission: boolean;
};

const GeneralSettings: FC<Props> = ({
  realmDescription,
  loginInfoUrl,
  pluginUrl,
  anonymousMode,
  alertsUrl,
  releaseFeedUrl,
  mailDomainName,
  enabledXsrfProtection,
  emergencyContacts,
  namespaceStrategy,
  namespaceStrategies,
  onChange,
  hasUpdatePermission,
}) => {
  const { t } = useTranslation("config");
  const [query, setQuery] = useState("");
  const { data: userOptions, isLoading: userOptionsLoading } = useUserOptions(query);

  const handleLoginInfoUrlChange = (value: string) => {
    onChange(true, value, "loginInfoUrl");
  };
  const handlePluginCenterUrlChange = (value: string) => {
    onChange(true, value, "pluginUrl");
  };
  const handleRealmDescriptionChange = (value: string) => {
    onChange(true, value, "realmDescription");
  };
  const handleEnabledXsrfProtectionChange = (value: boolean) => {
    onChange(true, value, "enabledXsrfProtection");
  };
  const handleAnonymousMode = (value: string) => {
    onChange(true, value as AnonymousMode, "anonymousMode");
  };
  const handleNamespaceStrategyChange = (value: string) => {
    onChange(true, value, "namespaceStrategy");
  };
  const handleAlertsUrlChange = (value: string) => {
    onChange(true, value, "alertsUrl");
  };
  const handleReleaseFeedUrlChange = (value: string) => {
    onChange(true, value, "releaseFeedUrl");
  };
  const handleMailDomainNameChange = (value: string) => {
    onChange(true, value, "mailDomainName");
  };
  const handleEmergencyContactsChange = (p: Option<AutocompleteObject>[]) => {
    onChange(
      true,
      p.map((c) => c.value.id),
      "emergencyContacts"
    );
  };

  return (
    <div>
      <div className="columns">
        <div className="column">
          <InputField
            label={t("general-settings.realm-description")}
            onChange={(event) => handleRealmDescriptionChange(event.target.value)}
            value={realmDescription}
            disabled={!hasUpdatePermission}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <NamespaceStrategySelect
            label={t("general-settings.namespace-strategy")}
            onChange={handleNamespaceStrategyChange}
            value={namespaceStrategy}
            disabled={!hasUpdatePermission}
            namespaceStrategies={namespaceStrategies}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <InputField
            label={t("general-settings.login-info-url")}
            onChange={(event) => handleLoginInfoUrlChange(event.target.value)}
            value={loginInfoUrl}
            disabled={!hasUpdatePermission}
            descriptionText={t("help.loginInfoUrlHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <InputField
            label={t("general-settings.pluginUrl")}
            onChange={(event) => handlePluginCenterUrlChange(event.target.value)}
            value={pluginUrl}
            disabled={!hasUpdatePermission}
            descriptionText={t("help.pluginUrlHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <Field>
            <Label as="span">{t("general-settings.enabled-xsrf-protection")}</Label>
            <Checkbox
              label={t("general-settings.enabled-xsrf-protection")}
              onChange={(event) => handleEnabledXsrfProtectionChange(event.target.checked)}
              checked={enabledXsrfProtection}
              disabled={!hasUpdatePermission}
              helpText={t("help.enableXsrfProtectionHelpText")}
            />
          </Field>
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <SelectField
            label={t("general-settings.anonymousMode.title")}
            onChange={(event) => handleAnonymousMode(event.target.value)}
            value={anonymousMode}
            disabled={!hasUpdatePermission}
            options={[
              { label: t("general-settings.anonymousMode.full"), value: "FULL" },
              { label: t("general-settings.anonymousMode.protocolOnly"), value: "PROTOCOL_ONLY" },
              { label: t("general-settings.anonymousMode.off"), value: "OFF" },
            ]}
            helpText={t("help.allowAnonymousAccessHelpText")}
            testId="anonymous-mode-select"
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <InputField
            label={t("general-settings.mail-domain-name")}
            onChange={(event) => handleMailDomainNameChange(event.target.value)}
            value={mailDomainName}
            disabled={!hasUpdatePermission}
            descriptionText={t("help.mailDomainNameHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <InputField
            label={t("general-settings.alerts-url")}
            onChange={(event) => handleAlertsUrlChange(event.target.value)}
            value={alertsUrl}
            disabled={!hasUpdatePermission}
            descriptionText={t("help.alertsUrlHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column">
          <InputField
            label={t("general-settings.release-feed-url")}
            onChange={(event) => handleReleaseFeedUrlChange(event.target.value)}
            value={releaseFeedUrl}
            disabled={!hasUpdatePermission}
            descriptionText={t("help.releaseFeedUrlHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column is-full">
          <ChipInputField<AutocompleteObject>
            label={t("general-settings.emergencyContacts.label")}
            helpText={t("general-settings.emergencyContacts.helpText")}
            placeholder={t("general-settings.emergencyContacts.autocompletePlaceholder")}
            aria-label="general-settings.emergencyContacts.ariaLabel"
            value={emergencyContacts.map((m) => ({ label: m, value: { id: m, displayName: m } }))}
            onChange={handleEmergencyContactsChange}
          >
            <Combobox<AutocompleteObject>
              options={userOptions || []}
              className={classNames({ "is-loading": userOptionsLoading })}
              onQueryChange={setQuery}
            />
          </ChipInputField>
        </div>
      </div>
    </div>
  );
};

export default GeneralSettings;

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
import { useTranslation } from "react-i18next";
import { useUserSuggestions } from "@scm-manager/ui-api";
import { AnonymousMode, ConfigChangeHandler, NamespaceStrategies, SelectValue } from "@scm-manager/ui-types";
import {
  AutocompleteAddEntryToTableField,
  Checkbox,
  InputField,
  MemberNameTagGroup,
  Select
} from "@scm-manager/ui-components";
import NamespaceStrategySelect from "./NamespaceStrategySelect";

type Props = {
  realmDescription: string;
  loginInfoUrl: string;
  disableGroupingGrid: boolean;
  dateFormat: string;
  anonymousMode: AnonymousMode;
  skipFailedAuthenticators: boolean;
  alertsUrl: string;
  releaseFeedUrl: string;
  mailDomainName: string;
  enabledXsrfProtection: boolean;
  enabledUserConverter: boolean;
  enabledApiKeys: boolean;
  emergencyContacts: string[];
  namespaceStrategy: string;
  namespaceStrategies?: NamespaceStrategies;
  onChange: ConfigChangeHandler;
  hasUpdatePermission: boolean;
};

const GeneralSettings: FC<Props> = ({
  realmDescription,
  loginInfoUrl,
  anonymousMode,
  alertsUrl,
  releaseFeedUrl,
  mailDomainName,
  enabledXsrfProtection,
  enabledUserConverter,
  enabledApiKeys,
  emergencyContacts,
  namespaceStrategy,
  namespaceStrategies,
  onChange,
  hasUpdatePermission
}) => {
  const { t } = useTranslation("config");
  const userSuggestions = useUserSuggestions();

  const handleLoginInfoUrlChange = (value: string) => {
    onChange(true, value, "loginInfoUrl");
  };
  const handleRealmDescriptionChange = (value: string) => {
    onChange(true, value, "realmDescription");
  };
  const handleEnabledXsrfProtectionChange = (value: boolean) => {
    onChange(true, value, "enabledXsrfProtection");
  };
  const handleEnabledUserConverterChange = (value: boolean) => {
    onChange(true, value, "enabledUserConverter");
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
  const handleEnabledApiKeysChange = (value: boolean) => {
    onChange(true, value, "enabledApiKeys");
  };
  const handleEmergencyContactsChange = (p: string[]) => {
    onChange(true, p, "emergencyContacts");
  };

  const isMember = (name: string) => {
    return emergencyContacts.includes(name);
  };

  const addEmergencyContact = (value: SelectValue) => {
    if (isMember(value.value.id)) {
      return;
    }
    handleEmergencyContactsChange([...emergencyContacts, value.value.id]);
  };

  return (
    <div>
      <div className="columns">
        <div className="column is-half">
          <InputField
            label={t("general-settings.realm-description")}
            onChange={handleRealmDescriptionChange}
            value={realmDescription}
            disabled={!hasUpdatePermission}
            helpText={t("help.realmDescriptionHelpText")}
          />
        </div>
        <div className="column is-half">
          <NamespaceStrategySelect
            label={t("general-settings.namespace-strategy")}
            onChange={handleNamespaceStrategyChange}
            value={namespaceStrategy}
            disabled={!hasUpdatePermission}
            namespaceStrategies={namespaceStrategies}
            helpText={t("help.nameSpaceStrategyHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column is-half">
          <InputField
            label={t("general-settings.login-info-url")}
            onChange={handleLoginInfoUrlChange}
            value={loginInfoUrl}
            disabled={!hasUpdatePermission}
            helpText={t("help.loginInfoUrlHelpText")}
          />
        </div>
        <div className="column is-half">
          <Checkbox
            label={t("general-settings.enabled-xsrf-protection")}
            onChange={handleEnabledXsrfProtectionChange}
            checked={enabledXsrfProtection}
            title={t("general-settings.enabled-xsrf-protection")}
            disabled={!hasUpdatePermission}
            helpText={t("help.enableXsrfProtectionHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column is-half">
          <Select
            label={t("general-settings.anonymousMode.title")}
            onChange={handleAnonymousMode}
            value={anonymousMode}
            disabled={!hasUpdatePermission}
            className="is-fullwidth"
            options={[
              { label: t("general-settings.anonymousMode.full"), value: "FULL" },
              { label: t("general-settings.anonymousMode.protocolOnly"), value: "PROTOCOL_ONLY" },
              { label: t("general-settings.anonymousMode.off"), value: "OFF" }
            ]}
            helpText={t("help.allowAnonymousAccessHelpText")}
            testId={"anonymous-mode-select"}
          />
        </div>
        <div className="column is-half">
          <Checkbox
            label={t("general-settings.enabled-user-converter")}
            onChange={handleEnabledUserConverterChange}
            checked={enabledUserConverter}
            title={t("general-settings.enabled-user-converter")}
            disabled={!hasUpdatePermission}
            helpText={t("help.enabledUserConverterHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column is-half">
          <InputField
            label={t("general-settings.mail-domain-name")}
            onChange={handleMailDomainNameChange}
            value={mailDomainName}
            disabled={!hasUpdatePermission}
            helpText={t("help.mailDomainNameHelpText")}
          />
        </div>
        <div className="column is-half">
          <Checkbox
            label={t("general-settings.enabled-api-keys")}
            onChange={handleEnabledApiKeysChange}
            checked={enabledApiKeys}
            title={t("general-settings.enabled-api-keys")}
            disabled={!hasUpdatePermission}
            helpText={t("help.enabledApiKeysHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column is-half">
          <InputField
            label={t("general-settings.alerts-url")}
            onChange={handleAlertsUrlChange}
            value={alertsUrl}
            disabled={!hasUpdatePermission}
            helpText={t("help.alertsUrlHelpText")}
          />
        </div>
        <div className="column is-half">
          <InputField
            label={t("general-settings.release-feed-url")}
            onChange={handleReleaseFeedUrlChange}
            value={releaseFeedUrl}
            disabled={!hasUpdatePermission}
            helpText={t("help.releaseFeedUrlHelpText")}
          />
        </div>
      </div>
      <div className="columns">
        <div className="column is-full">
          <MemberNameTagGroup
            members={emergencyContacts}
            memberListChanged={handleEmergencyContactsChange}
            label={t("general-settings.emergencyContacts.label")}
            helpText={t("general-settings.emergencyContacts.helpText")}
          />
          <AutocompleteAddEntryToTableField
            addEntry={addEmergencyContact}
            buttonLabel={t("general-settings.emergencyContacts.addButton")}
            loadSuggestions={userSuggestions}
            placeholder={t("general-settings.emergencyContacts.autocompletePlaceholder")}
            helpText={t("general-settings.emergencyContacts.helpText")}
          />
        </div>
      </div>
    </div>
  );
};

export default GeneralSettings;

// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField } from "@scm-manager/ui-components";

type Props = {
  realmDescription: string,
  enableRepositoryArchive: boolean,
  disableGroupingGrid: boolean,
  dateFormat: string,
  anonymousAccessEnabled: boolean,
  skipFailedAuthenticators: boolean,
  pluginUrl: string,
  enabledXsrfProtection: boolean,
  defaultNamespaceStrategy: string,
  t: string => string,
  onChange: (boolean, any, string) => void,
  hasUpdatePermission: boolean
};

class GeneralSettings extends React.Component<Props> {
  render() {
    const {
      t,
      realmDescription,
      enableRepositoryArchive,
      disableGroupingGrid,
      dateFormat,
      anonymousAccessEnabled,
      skipFailedAuthenticators,
      pluginUrl,
      enabledXsrfProtection,
      defaultNamespaceStrategy,
      hasUpdatePermission
    } = this.props;

    return (
      <div>
        <InputField
          label={t("general-settings.realm-description")}
          onChange={this.handleRealmDescriptionChange}
          value={realmDescription}
          disabled={!hasUpdatePermission}
          helpText={"hallo"}
        />
        <InputField
          label={t("general-settings.date-format")}
          onChange={this.handleDateFormatChange}
          value={dateFormat}
          disabled={!hasUpdatePermission}
        />
        <InputField
          label={t("general-settings.plugin-url")}
          onChange={this.handlePluginUrlChange}
          value={pluginUrl}
          disabled={!hasUpdatePermission}
        />
        <InputField
          label={t("general-settings.default-namespace-strategy")}
          onChange={this.handleDefaultNamespaceStrategyChange}
          value={defaultNamespaceStrategy}
          disabled={!hasUpdatePermission}
        />
        <Checkbox
          checked={enabledXsrfProtection}
          label={t("general-settings.enabled-xsrf-protection")}
          onChange={this.handleEnabledXsrfProtectionChange}
          disabled={!hasUpdatePermission}
          helpText={"hey"}
        />
        <Checkbox
          checked={enableRepositoryArchive}
          label={t("general-settings.enable-repository-archive")}
          onChange={this.handleEnableRepositoryArchiveChange}
          disabled={!hasUpdatePermission}
        />
        <Checkbox
          checked={disableGroupingGrid}
          label={t("general-settings.disable-grouping-grid")}
          onChange={this.handleDisableGroupingGridChange}
          disabled={!hasUpdatePermission}
        />
        <Checkbox
          checked={anonymousAccessEnabled}
          label={t("general-settings.anonymous-access-enabled")}
          onChange={this.handleAnonymousAccessEnabledChange}
          disabled={!hasUpdatePermission}
        />
        <Checkbox
          checked={skipFailedAuthenticators}
          label={t("general-settings.skip-failed-authenticators")}
          onChange={this.handleSkipFailedAuthenticatorsChange}
          disabled={!hasUpdatePermission}
        />
      </div>
    );
  }

  handleRealmDescriptionChange = (value: string) => {
    this.props.onChange(true, value, "realmDescription");
  };
  handleEnableRepositoryArchiveChange = (value: boolean) => {
    this.props.onChange(true, value, "enableRepositoryArchive");
  };
  handleDisableGroupingGridChange = (value: boolean) => {
    this.props.onChange(true, value, "disableGroupingGrid");
  };
  handleDateFormatChange = (value: string) => {
    this.props.onChange(true, value, "dateFormat");
  };
  handleAnonymousAccessEnabledChange = (value: string) => {
    this.props.onChange(true, value, "anonymousAccessEnabled");
  };

  handleSkipFailedAuthenticatorsChange = (value: string) => {
    this.props.onChange(true, value, "skipFailedAuthenticators");
  };
  handlePluginUrlChange = (value: string) => {
    this.props.onChange(true, value, "pluginUrl");
  };

  handleEnabledXsrfProtectionChange = (value: boolean) => {
    this.props.onChange(true, value, "enabledXsrfProtection");
  };
  handleDefaultNamespaceStrategyChange = (value: string) => {
    this.props.onChange(true, value, "defaultNamespaceStrategy");
  };
}

export default translate("config")(GeneralSettings);

// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField, Select } from "@scm-manager/ui-components";
import type { NamespaceStrategies } from "@scm-manager/ui-types";
import NamespaceStrategySelect from "./NamespaceStrategySelect";

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
  namespaceStrategies?: NamespaceStrategies,
  onChange: (boolean, any, string) => void,
  hasUpdatePermission: boolean,
  // context props
  t: string => string
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
      hasUpdatePermission,
      namespaceStrategies
    } = this.props;

    return (
      <div>
          <div className="columns">
            <div className="column is-half">
                <InputField
                  label={t("general-settings.realm-description")}
                  onChange={this.handleRealmDescriptionChange}
                  value={realmDescription}
                  disabled={!hasUpdatePermission}
                  helpText={t("help.realmDescriptionHelpText")}
                />
                </div>
                <div className="column is-half">
                <InputField
                  label={t("general-settings.date-format")}
                  onChange={this.handleDateFormatChange}
                  value={dateFormat}
                  disabled={!hasUpdatePermission}
                  helpText={t("help.dateFormatHelpText")}
                />
                </div>
            </div>
            <div className="columns">
                <div className="column is-half">
                <InputField
                  label={t("general-settings.plugin-url")}
                  onChange={this.handlePluginUrlChange}
                  value={pluginUrl}
                  disabled={!hasUpdatePermission}
                  helpText={t("help.pluginRepositoryHelpText")}
                />
                </div>
                <div className="column is-half">
                  <NamespaceStrategySelect
                    label={t("general-settings.default-namespace-strategy")}
                    onChange={this.handleDefaultNamespaceStrategyChange}
                    value={defaultNamespaceStrategy}
                    disabled={!hasUpdatePermission}
                    namespaceStrategies={namespaceStrategies}
                    helpText={t("help.defaultNameSpaceStrategyHelpText")}
                  />
                </div>
            </div>
            <div className="columns">
                <div className="column is-half">
                    <Checkbox
                      checked={enabledXsrfProtection}
                      label={t("general-settings.enabled-xsrf-protection")}
                      onChange={this.handleEnabledXsrfProtectionChange}
                      disabled={!hasUpdatePermission}
                      helpText={t("help.enableXsrfProtectionHelpText")}
                    />
                </div>
                <div className="column is-half">
                    <Checkbox
                      checked={enableRepositoryArchive}
                      label={t("general-settings.enable-repository-archive")}
                      onChange={this.handleEnableRepositoryArchiveChange}
                      disabled={!hasUpdatePermission}
                      helpText={t("help.enableRepositoryArchiveHelpText")}
                    />
                </div>
            </div>
            <div className="columns">
                <div className="column is-half">
                    <Checkbox
                      checked={disableGroupingGrid}
                      label={t("general-settings.disable-grouping-grid")}
                      onChange={this.handleDisableGroupingGridChange}
                      disabled={!hasUpdatePermission}
                      helpText={t("help.disableGroupingGridHelpText")}
                    />
                </div>
                <div className="column is-half">
                    <Checkbox
                      checked={anonymousAccessEnabled}
                      label={t("general-settings.anonymous-access-enabled")}
                      onChange={this.handleAnonymousAccessEnabledChange}
                      disabled={!hasUpdatePermission}
                      helpText={t("help.allowAnonymousAccessHelpText")}
                    />
                </div>
            </div>
            <div className="columns">
                <div className="column is-half">
                    <Checkbox
                      checked={skipFailedAuthenticators}
                      label={t("general-settings.skip-failed-authenticators")}
                      onChange={this.handleSkipFailedAuthenticatorsChange}
                      disabled={!hasUpdatePermission}
                      helpText={t("help.skipFailedAuthenticatorsHelpText")}
                    />
                </div>
            </div>
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

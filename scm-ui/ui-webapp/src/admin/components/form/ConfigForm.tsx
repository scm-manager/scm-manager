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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Config, NamespaceStrategies } from "@scm-manager/ui-types";
import { Level, Notification, SubmitButton } from "@scm-manager/ui-components";
import ProxySettings from "./ProxySettings";
import GeneralSettings from "./GeneralSettings";
import BaseUrlSettings from "./BaseUrlSettings";
import LoginAttempt from "./LoginAttempt";

type Props = WithTranslation & {
  submitForm: (p: Config) => void;
  config?: Config;
  loading?: boolean;
  configReadPermission: boolean;
  configUpdatePermission: boolean;
  namespaceStrategies?: NamespaceStrategies;
};

type State = {
  config: Config;
  showNotification: boolean;
  error: {
    loginAttemptLimitTimeout: boolean;
    loginAttemptLimit: boolean;
  };
  changed: boolean;
};

class ConfigForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      config: {
        proxyPassword: null,
        proxyPort: 0,
        proxyServer: "",
        proxyUser: null,
        enableProxy: false,
        realmDescription: "",
        disableGroupingGrid: false,
        dateFormat: "",
        anonymousMode: "OFF",
        baseUrl: "",
        mailDomainName: "",
        forceBaseUrl: false,
        loginAttemptLimit: 0,
        proxyExcludes: [],
        skipFailedAuthenticators: false,
        pluginUrl: "",
        loginAttemptLimitTimeout: 0,
        enabledXsrfProtection: true,
        enabledUserConverter: false,
        namespaceStrategy: "",
        loginInfoUrl: "",
        _links: {}
      },
      showNotification: false,
      error: {
        loginAttemptLimitTimeout: false,
        loginAttemptLimit: false
      },
      changed: false
    };
  }

  componentDidMount() {
    const { config, configUpdatePermission } = this.props;
    if (config) {
      this.setState({
        ...this.state,
        config: {
          ...config
        }
      });
    }
    if (!configUpdatePermission) {
      this.setState({
        ...this.state,
        showNotification: true
      });
    }
  }

  submit = (event: Event) => {
    event.preventDefault();
    this.setState({
      changed: false
    });
    this.props.submitForm(this.state.config);
  };

  render() {
    const { loading, t, namespaceStrategies, configReadPermission, configUpdatePermission } = this.props;
    const config = this.state.config;

    let noPermissionNotification = null;

    if (!configReadPermission) {
      return <Notification type={"danger"} children={t("config.form.no-read-permission-notification")} />;
    }

    if (this.state.showNotification) {
      noPermissionNotification = (
        <Notification
          type={"info"}
          children={t("config.form.no-write-permission-notification")}
          onClose={() => this.onClose()}
        />
      );
    }

    return (
      <form onSubmit={this.submit}>
        {noPermissionNotification}
        <GeneralSettings
          namespaceStrategies={namespaceStrategies}
          loginInfoUrl={config.loginInfoUrl}
          realmDescription={config.realmDescription}
          disableGroupingGrid={config.disableGroupingGrid}
          dateFormat={config.dateFormat}
          anonymousMode={config.anonymousMode}
          skipFailedAuthenticators={config.skipFailedAuthenticators}
          pluginUrl={config.pluginUrl}
          releaseFeedUrl={config.releaseFeedUrl}
          mailDomainName={config.mailDomainName}
          enabledXsrfProtection={config.enabledXsrfProtection}
          enabledUserConverter={config.enabledUserConverter}
          enabledApiKeys={config.enabledApiKeys}
          namespaceStrategy={config.namespaceStrategy}
          onChange={(isValid, changedValue, name) => this.onChange(isValid, changedValue, name)}
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <LoginAttempt
          loginAttemptLimit={config.loginAttemptLimit}
          loginAttemptLimitTimeout={config.loginAttemptLimitTimeout}
          onChange={(isValid, changedValue, name) => this.onChange(isValid, changedValue, name)}
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <BaseUrlSettings
          baseUrl={config.baseUrl}
          forceBaseUrl={config.forceBaseUrl}
          onChange={(isValid, changedValue, name) => this.onChange(isValid, changedValue, name)}
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <ProxySettings
          proxyPassword={config.proxyPassword ? config.proxyPassword : ""}
          proxyPort={config.proxyPort}
          proxyServer={config.proxyServer ? config.proxyServer : ""}
          proxyUser={config.proxyUser ? config.proxyUser : ""}
          enableProxy={config.enableProxy}
          proxyExcludes={config.proxyExcludes}
          onChange={(isValid, changedValue, name) => this.onChange(isValid, changedValue, name)}
          hasUpdatePermission={configUpdatePermission}
        />
        <hr />
        <Level
          right={
            <SubmitButton
              loading={loading}
              label={t("config.form.submit")}
              disabled={!configUpdatePermission || this.hasError() || !this.state.changed}
            />
          }
        />
      </form>
    );
  }

  onChange = (isValid: boolean, changedValue: any, name: string) => {
    this.setState({
      ...this.state,
      config: {
        ...this.state.config,
        [name]: changedValue
      },
      error: {
        ...this.state.error,
        [name]: !isValid
      },
      changed: true
    });
  };

  hasError = () => {
    return this.state.error.loginAttemptLimit || this.state.error.loginAttemptLimitTimeout;
  };

  onClose = () => {
    this.setState({
      ...this.state,
      showNotification: false
    });
  };
}

export default withTranslation("config")(ConfigForm);

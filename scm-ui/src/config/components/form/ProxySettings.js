// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField } from "../../../components/forms/index";
import Subtitle from "../../../components/layout/Subtitle";
import ProxyExcludesTable from "../table/ProxyExcludesTable";
import AddProxyExcludeField from "../fields/AddProxyExcludeField";

type Props = {
  proxyPassword: string,
  proxyPort: number,
  proxyServer: string,
  proxyUser: string,
  enableProxy: boolean,
  proxyExcludes: string[], //TODO: einbauen!
  t: string => string,
  onChange: (boolean, any, string) => void
};

class ProxySettings extends React.Component<Props> {
  render() {
    const {
      t,
      proxyPassword,
      proxyPort,
      proxyServer,
      proxyUser,
      enableProxy,
      proxyExcludes
    } = this.props;

    return (
      <div>
        <Subtitle subtitle={t("proxy-settings.name")} />
        <Checkbox
          checked={enableProxy}
          label={t("proxy-settings.enable-proxy")}
          onChange={this.handleEnableProxyChange}
        />
        <InputField
          label={t("proxy-settings.proxy-password")}
          onChange={this.handleProxyPasswordChange}
          value={proxyPassword}
          disable={!enableProxy}
        />
        <InputField
          label={t("proxy-settings.proxy-port")}
          value={proxyPort}
          onChange={this.handleProxyPortChange}
          disable={!enableProxy}
        />
        <InputField
          label={t("proxy-settings.proxy-server")}
          value={proxyServer}
          onChange={this.handleProxyServerChange}
          disable={!enableProxy}
        />
        <InputField
          label={t("proxy-settings.proxy-user")}
          value={proxyUser}
          onChange={this.handleProxyUserChange}
          disable={!enableProxy}
        />
        <ProxyExcludesTable
          proxyExcludes={proxyExcludes}
          onChange={(isValid, changedValue, name) =>
            this.props.onChange(isValid, changedValue, name)
          }
        />
        <AddProxyExcludeField addProxyExclude={this.addProxyExclude} />
      </div>
    );
  }

  handleProxyPasswordChange = (value: string) => {
    this.props.onChange(true, value, "proxyPassword");
  };
  handleProxyPortChange = (value: string) => {
    this.props.onChange(true, value, "proxyPort");
  };
  handleProxyServerChange = (value: string) => {
    this.props.onChange(true, value, "proxyServer");
  };
  handleProxyUserChange = (value: string) => {
    this.props.onChange(true, value, "proxyUser");
  };
  handleEnableProxyChange = (value: string) => {
    this.props.onChange(true, value, "enableProxy");
  };

  addProxyExclude = (proxyExcludeName: string) => {
    if (this.isProxyExcludeMember(proxyExcludeName)) {
      return;
    }
    this.props.onChange(
      true,
      [...this.props.proxyExcludes, proxyExcludeName],
      "proxyExcludes"
    );
  };

  isProxyExcludeMember = (proxyExcludeName: string) => {
    return this.props.proxyExcludes.includes(proxyExcludeName);
  };
}

export default translate("config")(ProxySettings);

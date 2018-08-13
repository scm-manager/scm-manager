// @flow
import React from "react";
import { translate } from "react-i18next";
import { Checkbox, InputField } from "../../../components/forms/index";
import Subtitle from "../../../components/layout/Subtitle";

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
      enableProxy
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
}

export default translate("config")(ProxySettings);

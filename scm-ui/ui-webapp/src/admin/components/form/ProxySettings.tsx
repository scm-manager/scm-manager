import React from 'react';
import { translate } from 'react-i18next';
import {
  Checkbox,
  InputField,
  Subtitle,
  AddEntryToTableField,
} from '@scm-manager/ui-components';
import ProxyExcludesTable from '../table/ProxyExcludesTable';

type Props = {
  proxyPassword: string;
  proxyPort: number;
  proxyServer: string;
  proxyUser: string;
  enableProxy: boolean;
  proxyExcludes: string[];
  t: (p: string) => string;
  onChange: (p1: boolean, p2: any, p3: string) => void;
  hasUpdatePermission: boolean;
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
      proxyExcludes,
      hasUpdatePermission,
    } = this.props;

    return (
      <div>
        <Subtitle subtitle={t('proxy-settings.name')} />
        <div className="columns">
          <div className="column is-full">
            <Checkbox
              checked={enableProxy}
              label={t('proxy-settings.enable-proxy')}
              onChange={this.handleEnableProxyChange}
              disabled={!hasUpdatePermission}
              helpText={t('help.enableProxyHelpText')}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              label={t('proxy-settings.proxy-password')}
              onChange={this.handleProxyPasswordChange}
              value={proxyPassword}
              type="password"
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t('help.proxyPasswordHelpText')}
            />
          </div>
          <div className="column is-half">
            <InputField
              label={t('proxy-settings.proxy-port')}
              value={proxyPort}
              onChange={this.handleProxyPortChange}
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t('help.proxyPortHelpText')}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              label={t('proxy-settings.proxy-server')}
              value={proxyServer}
              onChange={this.handleProxyServerChange}
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t('help.proxyServerHelpText')}
            />
          </div>
          <div className="column is-half">
            <InputField
              label={t('proxy-settings.proxy-user')}
              value={proxyUser}
              onChange={this.handleProxyUserChange}
              disabled={!enableProxy || !hasUpdatePermission}
              helpText={t('help.proxyUserHelpText')}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full">
            <ProxyExcludesTable
              proxyExcludes={proxyExcludes}
              onChange={(isValid, changedValue, name) =>
                this.props.onChange(isValid, changedValue, name)
              }
              disabled={!enableProxy || !hasUpdatePermission}
            />
            <AddEntryToTableField
              addEntry={this.addProxyExclude}
              disabled={!enableProxy || !hasUpdatePermission}
              buttonLabel={t('proxy-settings.add-proxy-exclude-button')}
              fieldLabel={t('proxy-settings.add-proxy-exclude-textfield')}
              errorMessage={t('proxy-settings.add-proxy-exclude-error')}
            />
          </div>
        </div>
      </div>
    );
  }

  handleProxyPasswordChange = (value: string) => {
    this.props.onChange(true, value, 'proxyPassword');
  };
  handleProxyPortChange = (value: string) => {
    this.props.onChange(true, value, 'proxyPort');
  };
  handleProxyServerChange = (value: string) => {
    this.props.onChange(true, value, 'proxyServer');
  };
  handleProxyUserChange = (value: string) => {
    this.props.onChange(true, value, 'proxyUser');
  };
  handleEnableProxyChange = (value: string) => {
    this.props.onChange(true, value, 'enableProxy');
  };

  addProxyExclude = (proxyExcludeName: string) => {
    if (this.isProxyExcludeMember(proxyExcludeName)) {
      return;
    }
    this.props.onChange(
      true,
      [...this.props.proxyExcludes, proxyExcludeName],
      'proxyExcludes',
    );
  };

  isProxyExcludeMember = (proxyExcludeName: string) => {
    return this.props.proxyExcludes.includes(proxyExcludeName);
  };
}

export default translate('config')(ProxySettings);

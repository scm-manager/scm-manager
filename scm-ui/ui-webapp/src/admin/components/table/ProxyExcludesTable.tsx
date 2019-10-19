import React from 'react';
import { translate } from 'react-i18next';
import ArrayConfigTable from './ArrayConfigTable';

type Props = {
  proxyExcludes: string[];
  t: (p: string) => string;
  onChange: (p1: boolean, p2: any, p3: string) => void;
  disabled: boolean;
};

type State = {};

class ProxyExcludesTable extends React.Component<Props, State> {
  render() {
    const { proxyExcludes, disabled, t } = this.props;
    return (
      <ArrayConfigTable
        items={proxyExcludes}
        label={t('proxy-settings.proxy-excludes')}
        removeLabel={t('proxy-settings.remove-proxy-exclude-button')}
        onRemove={this.removeEntry}
        disabled={disabled}
        helpText={t('help.proxyExcludesHelpText')}
      />
    );
  }

  removeEntry = (newExcludes: string[]) => {
    this.props.onChange(true, newExcludes, 'proxyExcludes');
  };
}

export default translate('config')(ProxyExcludesTable);

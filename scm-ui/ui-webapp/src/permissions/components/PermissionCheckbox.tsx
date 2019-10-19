import React from 'react';
import { translate } from 'react-i18next';
import { Checkbox } from '@scm-manager/ui-components';

type Props = {
  permission: string;
  checked: boolean;
  onChange: (value: boolean, name: string) => void;
  disabled: boolean;
  t: (p: string) => string;
};

class PermissionCheckbox extends React.Component<Props> {
  render() {
    const { t, permission, checked, onChange, disabled } = this.props;
    const key = permission.split(':').join('.');
    return (
      <Checkbox
        name={permission}
        label={this.translateOrDefault(
          'permissions.' + key + '.displayName',
          key,
        )}
        checked={checked}
        onChange={onChange}
        disabled={disabled}
        helpText={this.translateOrDefault(
          'permissions.' + key + '.description',
          t('permissions.unknown'),
        )}
      />
    );
  }

  translateOrDefault = (key: string, defaultText: string) => {
    const translation = this.props.t(key);
    if (translation === key) {
      return defaultText;
    } else {
      return translation;
    }
  };
}

export default translate('plugins')(PermissionCheckbox);

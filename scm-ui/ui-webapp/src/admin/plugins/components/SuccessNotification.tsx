import React from 'react';
import { translate } from 'react-i18next';
import { Notification } from '@scm-manager/ui-components';

type Props = {
  // context props
  t: (p: string) => string;
};

class InstallSuccessNotification extends React.Component<Props> {
  render() {
    const { t } = this.props;
    return (
      <Notification type="success">
        {t('plugins.modal.successNotification')}{' '}
        <a onClick={e => window.location.reload(true)}>
          {t('plugins.modal.reload')}
        </a>
      </Notification>
    );
  }
}

export default translate('admin')(InstallSuccessNotification);

import React from 'react';
import { Repository } from '@scm-manager/ui-types';
import { NavLink } from '@scm-manager/ui-components';
import { translate } from 'react-i18next';

type Props = {
  repository: Repository;
  editUrl: string;
  t: (p: string) => string;
};

class EditRepoNavLink extends React.Component<Props> {
  isEditable = () => {
    return this.props.repository._links.update;
  };

  render() {
    const { editUrl, t } = this.props;

    if (!this.isEditable()) {
      return null;
    }
    return (
      <NavLink to={editUrl} label={t('repositoryRoot.menu.generalNavLink')} />
    );
  }
}

export default translate('repos')(EditRepoNavLink);

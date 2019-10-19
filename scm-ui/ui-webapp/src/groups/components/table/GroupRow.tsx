import React from 'react';
import { translate } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Group } from '@scm-manager/ui-types';
import { Icon } from '@scm-manager/ui-components';

type Props = {
  group: Group;

  // context props
  t: (p: string) => string;
};

class GroupRow extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { group, t } = this.props;
    const to = `/group/${group.name}`;
    const iconType = group.external ? (
      <Icon title={t('group.external')} name="sign-out-alt fa-rotate-270" />
    ) : (
      <Icon title={t('group.internal')} name="sign-in-alt fa-rotate-90" />
    );

    return (
      <tr>
        <td>
          {iconType} {this.renderLink(to, group.name)}
        </td>
        <td className="is-hidden-mobile">{group.description}</td>
      </tr>
    );
  }
}

export default translate('groups')(GroupRow);

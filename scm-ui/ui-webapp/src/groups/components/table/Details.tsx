import React from 'react';
import { translate } from 'react-i18next';
import { Group } from '@scm-manager/ui-types';
import { DateFromNow, Checkbox } from '@scm-manager/ui-components';
import GroupMember from './GroupMember';

type Props = {
  group: Group;

  // Context props
  t: (p: string) => string;
};

class Details extends React.Component<Props> {
  render() {
    const { group, t } = this.props;
    return (
      <table className="table content">
        <tbody>
          <tr>
            <th>{t('group.name')}</th>
            <td>{group.name}</td>
          </tr>
          <tr>
            <th>{t('group.description')}</th>
            <td>{group.description}</td>
          </tr>
          <tr>
            <th>{t('group.external')}</th>
            <td>
              <Checkbox checked={group.external} />
            </td>
          </tr>
          <tr>
            <th>{t('group.type')}</th>
            <td>{group.type}</td>
          </tr>
          <tr>
            <th>{t('group.creationDate')}</th>
            <td>
              <DateFromNow date={group.creationDate} />
            </td>
          </tr>
          <tr>
            <th>{t('group.lastModified')}</th>
            <td>
              <DateFromNow date={group.lastModified} />
            </td>
          </tr>
          {this.renderMembers()}
        </tbody>
      </table>
    );
  }

  renderMembers() {
    const { group, t } = this.props;

    let member = null;
    if (group.members.length > 0) {
      member = (
        <tr>
          <th>{t('group.members')}</th>
          <td className="is-paddingless">
            <ul>
              {group._embedded.members.map((member, index) => {
                return <GroupMember key={index} member={member} />;
              })}
            </ul>
          </td>
        </tr>
      );
    }
    return member;
  }
}

export default translate('groups')(Details);

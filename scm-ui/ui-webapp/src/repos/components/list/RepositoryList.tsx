import React from 'react';

import { Repository } from '@scm-manager/ui-types';

import groupByNamespace from './groupByNamespace';
import RepositoryGroupEntry from './RepositoryGroupEntry';

type Props = {
  repositories: Repository[];
};

class RepositoryList extends React.Component<Props> {
  render() {
    const { repositories } = this.props;

    const groups = groupByNamespace(repositories);
    return (
      <div className="content">
        {groups.map(group => {
          return <RepositoryGroupEntry group={group} key={group.name} />;
        })}
      </div>
    );
  }
}

export default RepositoryList;

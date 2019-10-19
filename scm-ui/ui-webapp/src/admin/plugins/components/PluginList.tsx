import React from 'react';
import { Plugin } from '@scm-manager/ui-types';
import PluginGroupEntry from '../components/PluginGroupEntry';
import groupByCategory from './groupByCategory';

type Props = {
  plugins: Plugin[];
  refresh: () => void;
};

class PluginList extends React.Component<Props> {
  render() {
    const { plugins, refresh } = this.props;

    const groups = groupByCategory(plugins);
    return (
      <div className="content is-plugin-page">
        {groups.map(group => {
          return (
            <PluginGroupEntry
              group={group}
              key={group.name}
              refresh={refresh}
            />
          );
        })}
      </div>
    );
  }
}

export default PluginList;

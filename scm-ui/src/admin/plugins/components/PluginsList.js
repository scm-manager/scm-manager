//@flow
import React from "react";
import type { Plugins } from "@scm-manager/ui-types";

type Props = {
  plugins: Plugins[]
};

class RepositoryList extends React.Component<Props> {
  render() {
    const { plugins } = this.props;

    const groups = groupByNamespace(plugins);
    return (
      <div className="content">
        {groups.map(group => {
          return <PluginEntry group={group} key={group.name} />;
        })}
      </div>
    );
  }
}

export default RepositoryList;

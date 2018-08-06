//@flow
import React from "react";

import type { Repository } from "../../types/Repositories";

import groupByNamespace from "./groupByNamespace";
import RepositoryGroupEntry from "./RepositoryGroupEntry";

type Props = {
  repositories: Repository[]
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

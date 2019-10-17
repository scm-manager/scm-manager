//@flow
import React from "react";
import { CardColumnGroup } from "@scm-manager/ui-components";
import type { RepositoryGroup } from "@scm-manager/ui-types";
import RepositoryEntry from "./RepositoryEntry";

type Props = {
  group: RepositoryGroup
};

class RepositoryGroupEntry extends React.Component<Props> {
  render() {
    const { group } = this.props;
    const entries = group.repositories.map((repository, index) => {
      return <RepositoryEntry repository={repository} key={index} />;
    });
    return <CardColumnGroup name={group.name} elements={entries} />;
  }
}

export default RepositoryGroupEntry;

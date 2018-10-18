//@flow

import { Link } from "react-router-dom";
import React from "react";
import type { Repository, Changeset } from "@scm-manager/ui-types";

type Props = {
  repository: Repository,
  changeset: Changeset
};

export default class ChangesetId extends React.Component<Props> {
  render() {
    const { repository, changeset } = this.props;
    return (
      <Link
        to={`/repo/${repository.namespace}/${repository.name}/changeset/${
          changeset.id
        }`}
      >
        {changeset.id.substr(0, 7)}
      </Link>
    );
  }
}

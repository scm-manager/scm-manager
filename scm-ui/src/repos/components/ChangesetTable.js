// @flow
import ChangesetRow from "./ChangesetRow";
import React from "react";
import type { Changeset, Repository } from "@scm-manager/ui-types";
import classNames from "classnames";

type Props = {
  repository: Repository,
  changesets: Changeset[]
};

class ChangesetTable extends React.Component<Props> {
  render() {
    const { repository, changesets } = this.props;
    const content = changesets.map((changeset, index) => {
      return <ChangesetRow key={index} repository={repository} changeset={changeset} />;
    });
    return <div className={classNames("box")}>{content}</div>;
  }
}

export default ChangesetTable;

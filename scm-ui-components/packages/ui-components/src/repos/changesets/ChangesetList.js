// @flow
import ChangesetRow from "./ChangesetRow";
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";

import type { Changeset, Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository,
  changesets: Changeset[],
  classes: any
};

const styles = {
  toCenterContent: {
    display: "block"
  }
};

class ChangesetList extends React.Component<Props> {
  render() {
    const { repository, changesets, classes } = this.props;
    const content = changesets.map(changeset => {
      return (
        <ChangesetRow
          key={changeset.id}
          repository={repository}
          changeset={changeset}
        />
      );
    });
    return (
      <div className={classNames("panel-block", classes.toCenterContent)}>
        {content}
      </div>
    );
  }
}

export default injectSheet(styles)(ChangesetList);

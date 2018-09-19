// @flow
import ChangesetRow from "./ChangesetRow";
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import classNames from "classnames";

type Props = {
  changesets: Changeset[]
};

class ChangesetTable extends React.Component<Props> {
  render() {
    const { changesets } = this.props;
    const content = changesets.map((changeset, index) => {
      return <ChangesetRow key={index} changeset={changeset} />;
    });
    return <div className={classNames("box")}>{content}</div>;
  }
}

export default ChangesetTable;

//@flow

import React from "react";
import type { Changeset } from "@scm-manager/ui-types";

type Props = {
  changeset: Changeset
};

export default class ChangesetAuthor extends React.Component<Props> {
  render() {
    const { changeset } = this.props;
    return (
      <>
        {changeset.author.name}{" "}
        <a
          className="is-hidden-mobile"
          href={"mailto:" + changeset.author.mail}
        >
          &lt;
          {changeset.author.mail}
          &gt;
        </a>
      </>
    );
  }
}

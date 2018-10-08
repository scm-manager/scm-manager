//@flow

import React from "react";
import type { Changeset } from "@scm-manager/ui-types";

type Props = {
  changeset: Changeset
};

export default class ChangesetAuthor extends React.Component<Props> {
  render() {
    const { name } = this.props.changeset.author;

    return (
      <>
        {name} {this.renderMail()}
      </>
    );
  }

  renderMail() {
    const { mail } = this.props.changeset.author;
    if (mail) {
      return (
        <a className="is-hidden-mobile" href={"mailto:" + mail}>
          &lt;
          {mail}
          &gt;
        </a>
      );
    }
  }
}

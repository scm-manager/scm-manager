//@flow
import React from "react";
import type {Changeset} from "@scm-manager/ui-types";

type Props = {
  changeset: Changeset
};

class ChangesetAuthor extends React.Component<Props> {
  render() {
    const { changeset } = this.props;
    if (!changeset.author) {
      return null;
    }

    const { name } = changeset.author;
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

export default ChangesetAuthor;

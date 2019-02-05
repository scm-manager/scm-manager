//@flow
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import { ExtensionPoint } from "@scm-manager/ui-extensions";

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
        {name} {this.renderMail()} {this.renderAuthorMetadataExtensionPoint()}
      </>
    );
  }

  renderAuthorMetadataExtensionPoint = () => {
    const { changeset } = this.props;
    return (
      <ExtensionPoint
        name="changesets.changeset.author.metadata"
        props={{ changeset }}
        renderAll={true}
      >
        asas
      </ExtensionPoint>
    );
  };

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

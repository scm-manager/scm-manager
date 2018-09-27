//@flow
import React from "react";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Changeset, Repository } from "@scm-manager/ui-types";
import { Image } from "@scm-manager/ui-components";

type Props = {
  changeset: Changeset,
  repository: Repository
};

class ChangesetAvatar extends React.Component<Props> {
  render() {
    const { changeset, repository } = this.props;
    return (
      <p className="image is-64x64">
        <ExtensionPoint
          name="repos.changeset-table.information"
          props={{ changeset, repository }}
        >
        </ExtensionPoint>
      </p>
    );
  }
}

export default ChangesetAvatar;

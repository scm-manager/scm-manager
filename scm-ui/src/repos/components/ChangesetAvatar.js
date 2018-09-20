//@flow
import React from "react";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Changeset } from "@scm-manager/ui-types";
import { Image } from "@scm-manager/ui-components";

type Props = {
  changeset: Changeset
};

class ChangesetAvatar extends React.Component<Props> {
  render() {
    const { changeset } = this.props;
    return (
      <p className="image is-64x64">
        <ExtensionPoint
          name="repos.changeset-table.information"
          renderAll={true}
          props={{ changeset }}
        >
          <Image src="/images/blib.jpg" alt="Logo" />
        </ExtensionPoint>
      </p>
    );
  }
}

export default ChangesetAvatar;

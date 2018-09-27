//@flow
import React from "react";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Changeset } from "../../../../../scm-ui-components/packages/ui-types/src/index";
import { Image } from "../../../../../scm-ui-components/packages/ui-components/src/index";

type Props = {
  changeset: Changeset
};

class ChangesetAvatar extends React.Component<Props> {
  render() {
    const { changeset } = this.props;
    return (
      <div className="image is-64x64">
        <ExtensionPoint
          name="repos.changeset-table.information"
          renderAll={true}
          props={{ changeset }}
        >
          <Image src="/images/blib.jpg" alt="Logo" />
        </ExtensionPoint>
      </div>
    );
  }
}

export default ChangesetAvatar;
